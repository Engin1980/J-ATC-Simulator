package eng.jAtcSim.newLib.xml.area.internal.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ApproachXmlLoader extends XmlLoader<IList<Approach>> {

  private final static int MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH = 20;
  private final static int ENTRY_SECTOR_ONE_SIDE_ANGLE = 45;

  public static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

  public ApproachXmlLoader(Context context) {
    super(context);
  }

  @Override
  public IList<Approach> load(XElement source) {
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
    double expHeading = getOptimalEntryHeadingForRoute(route);
    int fromRadial = (int) Headings.add(expHeading, 115);
    int toRadial = (int) Headings.add(expHeading, 115);
    Coordinate coordinate;
    {
      String navaidName = ((ProceedDirectCommand) route.getRouteCommands().get(0)).getNavaidName();
      coordinate = context.area.navaids.get(navaidName).getCoordinate();
    }
    double maximalDistance = 10;
    FixRelatedLocation ret = FixRelatedLocation.create(
        coordinate, fromRadial, toRadial, maximalDistance);
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

  private double getOptimalEntryHeadingForRoute(IafRoute route) {
    EAssert.Argument.isNotNull(route);
    EAssert.Argument.isTrue(route.getRouteCommands().isEmpty() == false);
    EAssert.Argument.isTrue(route.getRouteCommands().get(0) instanceof ProceedDirectCommand);
    ProceedDirectCommand first = (ProceedDirectCommand) route.getRouteCommands().get(0);
    ICommand second = route.getRouteCommands().tryGetFirst(
        q -> q instanceof ProceedDirectCommand || q instanceof ChangeHeadingCommand);
    EAssert.isNotNull(
        second,
        sf("Iaf-Route does not contain change-heading or proceed-direct on second or later index in commands. Airport: %s, threshold %s",
            context.airport.icao,
            context.threshold.name));
    double ret;
    if (second instanceof ProceedDirectCommand) {
      Navaid a = context.area.navaids.get(first.getNavaidName());
      Navaid b = context.area.navaids.get(((ProceedDirectCommand) second).getNavaidName());
      ret = Coordinates.getBearing(a.getCoordinate(), b.getCoordinate());
    } else if (second instanceof ChangeHeadingCommand) {
      ret = ((ChangeHeadingCommand) second).getHeading();
    } else throw new UnsupportedOperationException();
    return ret;
  }

  private IList<Approach> loadCustom(XElement source) {
    throw new ToDoException("custom approaches in xml are not supported yet.");
  }

  private IList<Approach> loadGnss(XElement source) {
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int daA = XmlLoaderUtils.loadInteger("daA");
    int daB = XmlLoaderUtils.loadInteger("daB");
    int daC = XmlLoaderUtils.loadInteger("daC");
    int daD = XmlLoaderUtils.loadInteger("daD");
    int radial = XmlLoaderUtils.loadInteger("radial");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
    Double glidePathPercentage = XmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
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
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
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

    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int radial = XmlLoaderUtils.loadInteger("radial");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
    Double glidePathPercentage = XmlLoaderUtils.loadDouble("glidePathPercentage", 3d);
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
      XmlLoaderUtils.setContext(categorySource);
      ApproachType type;
      {
        String ilsType = XmlLoaderUtils.loadString("type");
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
      int daA = XmlLoaderUtils.loadInteger("daA");
      int daB = XmlLoaderUtils.loadInteger("daB");
      int daC = XmlLoaderUtils.loadInteger("daC");
      int daD = XmlLoaderUtils.loadInteger("daD");

      IList<ApproachStage> stages = new EList<>();
      { // radial descent stage
        ICondition exitCondition = AggregatingCondition.create(
            AggregatingCondition.eConditionAggregator.and,
            PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
            RunwayThresholdVisibilityCondition.create()
        );
        ICondition errorCondition = AggregatingCondition.create(
            AggregatingCondition.eConditionAggregator.or,
            PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(daA, daB, daC, daD)),
            PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(1000)),
            PlaneOrderedAltitudeDifferenceCondition.create(IntegerPerCategoryValue.create(-300))
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
    XmlLoaderUtils.setContext(source);
    String gaMapping = XmlLoaderUtils.loadString("gaMapping");
    String iafMapping = XmlLoaderUtils.loadString("iafMapping");
    int mdaA = XmlLoaderUtils.loadInteger("mdaA");
    int mdaB = XmlLoaderUtils.loadInteger("mdaB");
    int mdaC = XmlLoaderUtils.loadInteger("mdaC");
    int mdaD = XmlLoaderUtils.loadInteger("mdaD");
    int radial = XmlLoaderUtils.loadInteger("radial");
    ApproachType approachType = XmlLoaderUtils.loadEnum("type", ApproachType.class);
    EAssert.isTrue(approachType == ApproachType.ndb || approachType == ApproachType.vor);
    String fafName = XmlLoaderUtils.loadString("faf");
    String maptName = XmlLoaderUtils.loadString("mapt");
    int initialAltitude = XmlLoaderUtils.loadInteger("initialAltitude");
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
          PlaneShaCondition.createAsMinimalAltitude(IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD)),
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
