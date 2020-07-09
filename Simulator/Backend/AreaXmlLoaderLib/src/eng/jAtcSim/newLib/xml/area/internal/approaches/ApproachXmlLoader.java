package eng.jAtcSim.newLib.xml.area.internal.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ToDoException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ApproachXmlLoader extends XmlLoader<IList<Approach>> {

  private static class HeadingAndCoordinate {
    public final Coordinate coordinate;
    public final int heading;
    public final int range;

    public HeadingAndCoordinate(int heading, Coordinate coordinate, int range) {
      this.heading = heading;
      this.coordinate = coordinate;
      this.range = range;
    }
  }
  private final static int ENTRY_SECTOR_ONE_SIDE_ANGLE = 45;
  private final static int MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH = 20;

  public static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

  public ApproachXmlLoader(Context context) {
    super(context);
  }

  @Override
  public IList<Approach> load(XElement source) {
    log(4, "Xml-loading approach '%s'", source.getName());
    IList<Approach> ret;
    switch (source.getName()) {
      case "ilsApproach":
        ret = loadIls(source);
        break;
      case "gnssApproach":
        ret = loadGnss(source);
        break;
      case "unpreciseApproach":
        ret = loadUnprecise(source);
        break;
      case "customApproach":
        ret = loadCustom(source);
        break;
      default:
        throw new XmlLoadException("Unknown approach type " + source.getName() + ".");
    }
    return ret;
  }

  private ILocation createApproachEntryLocationBetweenMaptAndFaf(String maptName, String fafName) {
    Navaid faf = this.context.area.navaids.getWithPBD(fafName);
    Navaid mapt = this.context.area.navaids.getWithPBD(maptName);
    double distance = Coordinates.getDistanceInNM(faf.getCoordinate(), mapt.getCoordinate());
    double radial = Coordinates.getBearing(faf.getCoordinate(), mapt.getCoordinate());
    ILocation ret = FixRelatedLocation.create(mapt.getCoordinate(),
        (int) Headings.add(radial, -30),
        (int) Headings.add(radial, +30),
        distance + 1);
    return ret;
  }

  private ILocation createApproachEntryLocationForFafByFafName(String fafName, int course) {
    Navaid navaid = context.area.navaids.getWithPBD(fafName);
    ILocation ret = FixRelatedLocation.create(navaid.getCoordinate(),
        (int) Headings.add(course, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
        (int) Headings.add(course, ENTRY_SECTOR_ONE_SIDE_ANGLE),
        MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH);
    return ret;
  }

  private ILocation createApproachEntryLocationForRoute(IafRoute route) {
    //IDEA this should somehow allow set custom entry location?
    HeadingAndCoordinate hac = getOptimalEntryHeadingForRoute(route);
    int fromRadial = (int) Headings.add(hac.heading, -115);
    int toRadial = (int) Headings.add(hac.heading, 115);
    FixRelatedLocation ret = FixRelatedLocation.create(
        hac.coordinate, fromRadial, toRadial, hac.range);
    return ret;
  }

  private ILocation createApproachEntryLocationForThresholdAndSlope(
      Coordinate coordinate, int coordinateAltitude, int radial, int altitude, double slope) {
    double dist = (altitude - coordinateAltitude) / slope;
    Coordinate fafCoordinate = Coordinates.getCoordinate(
        coordinate, Headings.getOpposite(radial), dist);
    ILocation ret = FixRelatedLocation.create(fafCoordinate,
        (int) Headings.add(radial, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
        (int) Headings.add(radial, ENTRY_SECTOR_ONE_SIDE_ANGLE),
        MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH);
    return ret;
  }

  private HeadingAndCoordinate getOptimalEntryHeadingForRoute(IafRoute route) {
    EAssert.Argument.isNotNull(route);
    EAssert.Argument.isTrue(route.getRouteCommands().isEmpty() == false);

    HeadingAndCoordinate ret = null;

    for (int i = 0; i < route.getRouteCommands().size(); i++) {
      ICommand routeCommand = route.getRouteCommands().get(i);
      if (routeCommand instanceof FlyRadialBehavior) {
        FlyRadialBehavior flyRadialBehavior = (FlyRadialBehavior) routeCommand;
        ret = new HeadingAndCoordinate(
            flyRadialBehavior.getInboundRadial(),
            flyRadialBehavior.getCoordinate(),
            25);
        break;
      } else if (routeCommand instanceof ToNavaidCommand) {
        Navaid firstNavaid = AreaAcc.getNavaids().getWithPBD(((ToNavaidCommand) routeCommand).getNavaidName());
        Navaid secondNavaid = null;
        for (int j = i + 1; j < route.getRouteCommands().size(); j++) {
          if (route.getRouteCommands().get(j) instanceof ToNavaidCommand) {
            secondNavaid = AreaAcc.getNavaids().getWithPBD(((ToNavaidCommand) route.getRouteCommands().get(j)).getNavaidName());
            break;
          }
        }
        if (secondNavaid == null)
          ret = new HeadingAndCoordinate(
              (int) Coordinates.getBearing(firstNavaid.getCoordinate(), AreaAcc.getAirport().getLocation()),
              firstNavaid.getCoordinate(),
              15);
        else
          ret = new HeadingAndCoordinate(
              (int) Coordinates.getBearing(firstNavaid.getCoordinate(), secondNavaid.getCoordinate()),
              firstNavaid.getCoordinate(),
              10);
        break;
      }
    }

    EAssert.isNotNull(
        ret,
        sf("Unable to detect entry heading for iaf-route. Airport: %s, threshold %s",
            context.airport.icao,
            context.threshold.name));
    return ret;
  }

  private IList<Approach> loadCustom(XElement source) {
    throw new ToDoException("custom approaches in xml are not supported yet.");
  }

  private IList<Approach> loadGnss(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String gaMapping = SmartXmlLoaderUtils.loadString("gaMapping");
    String iafMapping = SmartXmlLoaderUtils.loadString("iafMapping");
    int daA = SmartXmlLoaderUtils.loadInteger("daA");
    int daB = SmartXmlLoaderUtils.loadInteger("daB");
    int daC = SmartXmlLoaderUtils.loadInteger("daC");
    int daD = SmartXmlLoaderUtils.loadInteger("daD");
    int radial = SmartXmlLoaderUtils.loadInteger("radial");
    int initialAltitude = SmartXmlLoaderUtils.loadInteger("initialAltitude");
    Double glidePathPercentage = SmartXmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
    double slope = convertGlidePathDegreesToSlope(glidePathPercentage);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = context.airport.iafMappings.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation entryLocation = createApproachEntryLocationForRoute(iafRoute);
      ApproachEntry entry = ApproachEntry.create(entryLocation, iafRoute);
      entries.add(entry);
    }

    // estimate faf by slope and daA
    {
      ILocation entryLocation = createApproachEntryLocationForThresholdAndSlope(
          context.threshold.coordinate, context.airport.altitude, radial, initialAltitude, slope);
      ApproachEntry entry = ApproachEntry.createDirect(entryLocation);
      entries.add(entry);
    }

    GaRoute gaRoute = context.airport.gaMappings.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
        ChangeAltitudeCommand.createDescend(initialAltitude));

    IList<ApproachStage> stages = new EList<>();
    { // radial descent stage
      ICondition exitCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.and,
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
          RunwayThresholdVisibilityCondition.create()
      );
      ICondition errorCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.or,
          PlaneShaCondition.createAsMaximalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
          PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(1000))
      );
      stages.add(ApproachStage.create(
          FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.altitude, slope),
          exitCondition,
          errorCondition,
          "GNSS radial " + context.airport.icao + ":" + context.threshold.name
      ));
    }
    {
      // landing stage
      stages.add(ApproachStage.create(
          LandingBehavior.create(),
          null,
          RunwayThresholdVisibilityCondition.create(),
          "GNSS landing " + context.airport.icao + ":" + context.threshold.name
      ));
    }

    Approach ret = new Approach(ApproachType.gnss, entries, beforeStagesCommands, stages, gaRoute);

    return EList.of(ret);
  }

  private IList<Approach> loadIls(XElement source) {
    IList<Approach> ret = new EList<>();

    SmartXmlLoaderUtils.setContext(source);
    String gaMapping = SmartXmlLoaderUtils.loadString("gaMapping");
    String iafMapping = SmartXmlLoaderUtils.loadString("iafMapping");
    int radial = SmartXmlLoaderUtils.loadInteger("radial");
    int initialAltitude = SmartXmlLoaderUtils.loadInteger("initialAltitude");
    Double glidePathPercentage = SmartXmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
    double slope = convertGlidePathDegreesToSlope(glidePathPercentage);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = context.airport.iafMappings.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation entryLocation = createApproachEntryLocationForRoute(iafRoute);
      ApproachEntry entry = ApproachEntry.create(entryLocation, iafRoute);
      entries.add(entry);
    }

    // direct FAF entry
    {
      ILocation entryLocation = createApproachEntryLocationForThresholdAndSlope(
          context.threshold.coordinate, context.airport.altitude, radial, initialAltitude, slope);
      ApproachEntry entry = ApproachEntry.createDirect(entryLocation);
      entries.add(entry);
    }

    GaRoute gaRoute = context.airport.gaMappings.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
        ChangeAltitudeCommand.createDescend(initialAltitude));

    for (XElement categorySource : source.getChild("categories").getChildren("category")) {
      SmartXmlLoaderUtils.setContext(categorySource);
      ApproachType type;
      {
        String ilsType = SmartXmlLoaderUtils.loadString("type");
        switch (ilsType) {
          case "I":
            type = ApproachType.ils_I;
            break;
          case "II":
            type = ApproachType.ils_II;
            break;
          case "III":
            type = ApproachType.ils_III;
            break;
          default:
            throw new EEnumValueUnsupportedException(ilsType);
        }
      }
      int daA = SmartXmlLoaderUtils.loadInteger("daA");
      int daB = SmartXmlLoaderUtils.loadInteger("daB");
      int daC = SmartXmlLoaderUtils.loadInteger("daC");
      int daD = SmartXmlLoaderUtils.loadInteger("daD");

      IList<ApproachStage> stages = new EList<>();
      { // radial descent stage
        ICondition exitCondition = AggregatingCondition.create(
            AggregatingCondition.eConditionAggregator.and,
            PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
            RunwayThresholdVisibilityCondition.create()
        );
        ICondition errorCondition = AggregatingCondition.create(
            AggregatingCondition.eConditionAggregator.or,
            PlaneShaCondition.createAsMaximalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)), // is below mda
            PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(1000)), // cannot be too high
            PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(-300))  // cannot be too low
        );
        stages.add(ApproachStage.create(
            FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.altitude, slope),
            exitCondition,
            errorCondition,
            type + " radial " + context.airport.icao + ":" + context.threshold.name
        ));
      }
      {
        // landing stage
        stages.add(ApproachStage.create(
            LandingBehavior.create(),
            null,
            RunwayThresholdVisibilityCondition.create(),
            type + " landing " + context.airport.icao + ":" + context.threshold.name
        ));
      }

      Approach tmp = new Approach(type, entries, beforeStagesCommands, stages, gaRoute);
      ret.add(tmp);
    }

    return ret;
  }

  private IList<Approach> loadUnprecise(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String gaMapping = SmartXmlLoaderUtils.loadString("gaMapping");
    String iafMapping = SmartXmlLoaderUtils.loadString("iafMapping");
    int mdaA = SmartXmlLoaderUtils.loadInteger("mdaA");
    int mdaB = SmartXmlLoaderUtils.loadInteger("mdaB");
    int mdaC = SmartXmlLoaderUtils.loadInteger("mdaC");
    int mdaD = SmartXmlLoaderUtils.loadInteger("mdaD");
    int radial = SmartXmlLoaderUtils.loadInteger("radial");
    ApproachType approachType = SmartXmlLoaderUtils.loadEnum("type", ApproachType.class);
    EAssert.isTrue(approachType == ApproachType.ndb || approachType == ApproachType.vor);
    String fafName = SmartXmlLoaderUtils.loadString("faf");
    String maptName = SmartXmlLoaderUtils.loadString("mapt");
    int initialAltitude = SmartXmlLoaderUtils.loadInteger("initialAltitude");
//    double slope = ?? get from trheshold +

    Navaid faf = context.area.navaids.getWithPBD(fafName);
    Navaid mapt = context.area.navaids.getWithPBD(maptName);

    // build approach entry
    IList<ApproachEntry> entries = new EList<>();
    IReadOnlyList<IafRoute> iafRoutes = context.airport.iafMappings.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ILocation entryLocation = createApproachEntryLocationForRoute(iafRoute);
      ApproachEntry entry = ApproachEntry.create(entryLocation, iafRoute);
      entries.add(entry);
    }

    // entry via faf before faf
    {
      ILocation entryLocation = createApproachEntryLocationForFafByFafName(fafName, radial);
      ApproachEntry entry = ApproachEntry.createDirect(entryLocation);
      entries.add(entry);
    }

    // entry before mapt to faf
    {
      ILocation entryLocation = createApproachEntryLocationBetweenMaptAndFaf(maptName, fafName);
      ApproachEntry entry = ApproachEntry.createDirect(entryLocation);
      entries.add(entry);
    }

    EAssert.isTrue(context.airport.gaMappings.get(gaMapping).count() > 0,
        sf("Approach has not defined ga-route matching mapping '%s'", gaMapping));
    GaRoute gaRoute = context.airport.gaMappings.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
        ChangeAltitudeCommand.createDescend(initialAltitude));

    IList<ApproachStage> stages = new EList<>();
    { // radial descent stage
      ICondition exitCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.and,
          LocationCondition.create(FixRelatedLocation.create(mapt.getCoordinate(), 0.5)),
          PlaneShaCondition.create(
              IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD),
              IntegerPerCategoryValue.create(mdaA + 500, mdaB + 500, mdaC + 500, mdaD + 500),
              null, null, null, null),
          RunwayThresholdVisibilityCondition.create()
      );
      ICondition errorCondition = AggregatingCondition.create(
          AggregatingCondition.eConditionAggregator.or,
          PlaneShaCondition.createAsMaximalAltitude(IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD)),
          PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(1000))
      );

      double slope =
          (initialAltitude - context.airport.altitude) / Coordinates.getDistanceInNM(faf.getCoordinate(), context.threshold.coordinate);
      stages.add(ApproachStage.create(
          FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.altitude, slope),
          exitCondition,
          errorCondition,
          approachType + " radial " + context.airport.icao + ":" + context.threshold.name
      ));
    }
    {
      // landing stage
      stages.add(ApproachStage.create(
          LandingBehavior.create(),
          null,
          RunwayThresholdVisibilityCondition.create(),
          approachType + " landing " + context.airport.icao + ":" + context.threshold.name
      ));
    }

    Approach ret = new Approach(approachType, entries, beforeStagesCommands, stages, gaRoute);

    return EList.of(ret);
  }
}
