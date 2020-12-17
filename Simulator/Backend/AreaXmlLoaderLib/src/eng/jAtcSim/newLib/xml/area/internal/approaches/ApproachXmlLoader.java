package eng.jAtcSim.newLib.xml.area.internal.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.ISet;
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
import eng.jAtcSim.newLib.area.approaches.ApproachErrorCondition;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyToPointWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.*;
import eng.jAtcSim.newLib.area.approaches.conditions.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.conditions.locations.RegionalLocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.shared.xml.XmlLoadException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ApproachXmlLoader extends XmlLoader<IList<Approach>> {

  private final static int ENTRY_SECTOR_ONE_SIDE_ANGLE = 45;
  private final static int MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH = 20;
  private static final int DEFAULT_SLOPE = 3;

  public static double convertGlidePathDegreesToSlope(double gpDegrees) {
    return Math.tan(Math.toRadians(gpDegrees));
  }

  private static double calculateTurnRadius(double speedInKt) {
    double distanceInTwoMinutes = speedInKt / 30d;
    double radius = distanceInTwoMinutes / 2 / Math.PI;
    return radius;
  }

  public ApproachXmlLoader(LoadingContext context) {
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
      case "defaultVisualApproach":
        ret = loadDefaultVisual();
        break;
      case "customApproach":
        ret = loadCustom(source);
        break;
      default:
        throw new XmlLoadException("Unknown approach type " + source.getName() + ".");
    }
    return ret;
  }

  private ICondition createApproachEntryConditionBetweenMaptAndFaf(String maptName, String fafName) {
    Navaid faf = this.context.area.navaids.getWithPBD(fafName);
    Navaid mapt = this.context.area.navaids.getWithPBD(maptName);
    double radial = Coordinates.getBearing(faf.getCoordinate(), mapt.getCoordinate());

    ICondition ret = AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
            FixRelatedLocation.create(mapt.getCoordinate(),
                    (int) Headings.add(radial, -30),
                    (int) Headings.add(radial, 30),
                    MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH),
            PlaneShaCondition.create(PlaneShaCondition.eType.heading,
                    (int) Headings.add(radial, -30),
                    (int) Headings.add(radial, 30)));

    return ret;
  }

  private ICondition createApproachEntryConditionForFafByFafName(String fafName, int course) {
    Navaid navaid = context.area.navaids.getWithPBD(fafName);

    ICondition ret = AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
            FixRelatedLocation.create(navaid.getCoordinate(),
                    (int) Headings.add(course, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    (int) Headings.add(course, ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH),
            PlaneShaCondition.create(PlaneShaCondition.eType.heading,
                    (int) Headings.add(course, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    (int) Headings.add(course, ENTRY_SECTOR_ONE_SIDE_ANGLE))
    );

    return ret;
  }

  private ICondition createApproachEntryConditionForThresholdAndSlope(
          Coordinate coordinate, int coordinateAltitude, int radial, int altitude, double slope) {
    double dist = (altitude - coordinateAltitude) / slope / 6076.1;
    Coordinate fafCoordinate = Coordinates.getCoordinate(
            coordinate, Headings.getOpposite(radial), dist);

    ICondition ret = AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
            FixRelatedLocation.create(fafCoordinate,
                    (int) Headings.add(radial, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    (int) Headings.add(radial, ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    MAXIMAL_DISTANCE_FROM_FAF_TO_ENTER_APPROACH),
            PlaneShaCondition.create(PlaneShaCondition.eType.heading,
                    (int) Headings.add(radial, -ENTRY_SECTOR_ONE_SIDE_ANGLE),
                    (int) Headings.add(radial, ENTRY_SECTOR_ONE_SIDE_ANGLE))
    );
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
      ApproachEntry entry = ApproachEntry.createIaf(iafRoute);
      entries.add(entry);
    }

    // estimate faf by slope and daA
    {
      ICondition entryConditionForThresholdAndSlope = createApproachEntryConditionForThresholdAndSlope(
              context.threshold.coordinate, context.airport.altitude, radial, initialAltitude, slope);
      ApproachEntry entry = ApproachEntry.create(entryConditionForThresholdAndSlope);
      entries.add(entry);
    }

    GaRoute gaRoute = context.airport.gaMappings.get(gaMapping).getFirst();
    IList<ICommand> beforeStagesCommands = EList.of(
            ChangeAltitudeCommand.createDescend(initialAltitude));

    IList<ApproachStage> stages = new EList<>();
    { // radial descent stage
      ICondition exitCondition = AggregatingCondition.create(
              AggregatingCondition.eConditionAggregator.and,
              PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, IntegerPerCategoryValue.create(daA, daB, daC, daD)),
              RunwayThresholdVisibleCondition.create()
      );
      ISet<ApproachErrorCondition> errs = ApproachErrorCondition.createSet(
              PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, IntegerPerCategoryValue.create(daA, daB, daC, daD)), GoingAroundNotification.GoAroundReason.decisionPointRunwayNotInSight,
              createAltitudeDifferenceRestriction(context.airport.altitude + 1800, 300, 500), GoingAroundNotification.GoAroundReason.unstabilizedAltitude,
              createNotStabilizedApproachErrorCondition(radial, context.airport.altitude + 1000, 16), GoingAroundNotification.GoAroundReason.unstabilizedHeading
      );
      stages.add(ApproachStage.create(
              "GNSS radial " + context.airport.icao + ":" + context.threshold.name,
              FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.declination, context.airport.altitude, slope),
              exitCondition,
              errs));
    }
    {
      // landing stage
      stages.add(ApproachStage.create(
              "GNSS landing " + context.airport.icao + ":" + context.threshold.name,
              LandingBehavior.create(),
              new NeverCondition()));
    }

    Approach ret = new Approach(ApproachType.gnss, initialAltitude, entries, beforeStagesCommands, stages, gaRoute);

    return EList.of(ret);
  }

  private IList<Approach> loadDefaultVisual() {
    IList<ApproachEntry> aes = new EList<>();

    double slope = convertGlidePathDegreesToSlope(DEFAULT_SLOPE);

    for (char c = 'A'; c <= 'E'; c++) {
      double fafAlt = c == 'A' ? 300 : c == 'B' ? 500 : c == 'C' ? 800 : c == 'D' ? 1200 : 1500;
      int circleAlt = context.airport.altitude +
              (c == 'A' ? 500 : c == 'B' ? 700 : c == 'C' ? 1000 : c == 'D' ? 2000 : 3000);
      double dist = fafAlt / slope / 6076.1;
      Coordinate faf = Coordinates.getCoordinate(context.threshold.coordinate, context.threshold.getOppositeCourse(), dist);
      ApproachEntry ae;
      ae = generateDefaultVisualAfterFaf(context.threshold, c, faf);
      aes.add(ae);
      ae = generateDefaultVisualNearbyInbound(context.threshold, c, faf);
      aes.add(ae);
      ae = generateDefaultVisualtNearbyOutbound(context.threshold, c, faf, circleAlt, LeftRight.left);
      aes.add(ae);
      ae = generateDefaultVisualtNearbyOutbound(context.threshold, c, faf, circleAlt, LeftRight.right);
      aes.add(ae);
      ae = generateDefaultVisualDownwind(context.threshold, c, faf, circleAlt, LeftRight.left);
      aes.add(ae);
      ae = generateDefaultVisualDownwind(context.threshold, c, faf, circleAlt, LeftRight.right);
      aes.add(ae);
    }

    IList<ApproachStage> stages = new EList<>();
    ISet<ApproachErrorCondition> errs = ApproachErrorCondition.createSet(
            PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, context.airport.altitude + 150), GoingAroundNotification.GoAroundReason.unstabilizedHeading
    );
    stages.add(ApproachStage.create(
            "Visual-default final descend",
            FlyRadialWithDescentBehavior.create(context.threshold.coordinate, context.threshold.course, context.airport.declination,
                    context.airport.altitude, convertGlidePathDegreesToSlope(DEFAULT_SLOPE)),
            AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
                    PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, context.airport.altitude + 150),
                    PlaneShaCondition.create(PlaneShaCondition.eType.heading,
                            (int) Headings.add(context.threshold.course, -16),
                            (int) Headings.add(context.threshold.course, 16))),
            errs
    ));
    stages.add(ApproachStage.create(
            "Visual-default landing",
            LandingBehavior.create(),
            new NeverCondition()
    ));

    GaRoute gaRoute = new GaRoute(EList.of(
            ChangeAltitudeCommand.create(context.airport.altitude + 1500),
            ChangeHeadingCommand.createContinueCurrentHeading()
    ));

    Approach app = new Approach(ApproachType.visual,
            context.airport.altitude + 1500,
            aes,
            new EList<>(),
            stages,
            gaRoute);

    IList<Approach> ret = new EList<>();
    ret.add(app);
    return ret;
  }

  private ApproachEntry generateDefaultVisualNearbyInbound(LoadingContext.ThresholdInfo threshold, char category, Coordinate faf) {
    int hdgMin = (int) Headings.add(threshold.course, -46);
    int hdgMax = (int) Headings.add(threshold.course, 46);

    ApproachEntry ret = ApproachEntry.create(
            AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
                    PlaneShaCondition.create(PlaneShaCondition.eType.heading, hdgMin, hdgMax),
                    FixRelatedLocation.create(threshold.coordinate,
                            hdgMin,
                            hdgMax,
                            8)),
            new PlaneCategoryDefinitions(category),
            new EList<>()
    ).withTag("Visual-app close final stage " + threshold.name);
    return ret;
  }

  private ApproachEntry generateDefaultVisualtNearbyOutbound(LoadingContext.ThresholdInfo threshold,
                                                             char category, Coordinate faf,
                                                             int altitude, LeftRight dir) {
    int boxAngle = dir == LeftRight.right ? -90 : 90;
    Coordinate a = threshold.coordinate;
    Coordinate b = faf;
    Coordinate c = Coordinates.getCoordinate(b, Headings.add(threshold.course, boxAngle), 15);
    Coordinate d = Coordinates.getCoordinate(a, Headings.add(threshold.course, boxAngle), 15);
    int hdgMin = dir == LeftRight.right ?
            (int) Headings.add(threshold.course, 90) :
            (int) Headings.add(threshold.course, 75);
    int hdgMax = dir == LeftRight.right ?
            (int) Headings.add(threshold.course, -75) :
            (int) Headings.add(threshold.course, -90);

    Coordinate downwindEndPoint = calculateDownwindEndPoint(category, faf, threshold.course, boxAngle);
    IList<ApproachStage> stages = getDefaultVisualDownwindStages(downwindEndPoint, threshold.course, altitude);

    ApproachEntry ret = ApproachEntry.create(
            AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
                    PlaneShaCondition.create(PlaneShaCondition.eType.heading, hdgMin, hdgMax),
                    RegionalLocation.create(a, b, c, d)),
            new PlaneCategoryDefinitions(category),
            stages
    ).withTag("Visual-app close downwind stage " + threshold.name + "/" + dir);

    return ret;
  }

  private IList<ApproachStage> getDefaultVisualDownwindStages(Coordinate downwindEndPoint, double runwayCourse, int downwindEndPointAltitude) {
    IList<ApproachStage> ret = new EList<>();

    ISet<ApproachErrorCondition> errs = ApproachErrorCondition.createSet(
            PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, downwindEndPointAltitude), GoingAroundNotification.GoAroundReason.unstabilizedHeading
    );

    ret.add(ApproachStage.create(
            "Visual-default downwind",
            FlyToPointWithDescentBehavior.create(downwindEndPoint,
                    downwindEndPointAltitude, convertGlidePathDegreesToSlope(DEFAULT_SLOPE)),
            FixRelatedLocation.create(downwindEndPoint,
                    (int) Headings.add(runwayCourse, 90),
                    (int) Headings.add(runwayCourse, -90),
                    50),
            errs
    ));
    return ret;
  }

  private ApproachEntry generateDefaultVisualAfterFaf(LoadingContext.ThresholdInfo threshold, char category, Coordinate faf) {
    ApproachEntry ret = ApproachEntry.create(
            FixRelatedLocation.create(faf,
                    (int) Headings.add(threshold.course, 91),
                    (int) Headings.add(threshold.course, -91),
                    15),
            new PlaneCategoryDefinitions(category),
            new EList<>()
    ).withTag("Visual-app direct to final stage " + threshold.name);
    return ret;
  }

  private ApproachEntry generateDefaultVisualDownwind(LoadingContext.ThresholdInfo threshold, char category, Coordinate faf, int altitude, LeftRight dir) {
    int aside = dir == LeftRight.right ? -90 : 90;

    Coordinate a = Coordinates.getCoordinate(threshold.coordinate, threshold.course, 15);
    Coordinate b = threshold.coordinate;
    Coordinate c = Coordinates.getCoordinate(b, Headings.add(threshold.course, aside), 15);
    Coordinate d = Coordinates.getCoordinate(a, Headings.add(threshold.course, aside), 15);

    Coordinate downwindEndPoint = calculateDownwindEndPoint(category, faf, threshold.course, aside);
    IList<ApproachStage> stages = getDefaultVisualDownwindStages(downwindEndPoint, threshold.course, altitude);

    ApproachEntry ae = ApproachEntry.create(RegionalLocation.create(a, b, c, d), new PlaneCategoryDefinitions(category), stages)
            .withTag("default visual downwind " + threshold.name + "/" + dir);
    return ae;
  }

  private Coordinate calculateDownwindEndPoint(char category, Coordinate faf, int runwayCourse, int asideHeading) {
    int expectedSpeed = category == 'A' ? 70 : category == 'B' ? 100 : category == 'C' ? 150 : category == 'D' ? 210 : 230;
    Coordinate ret = Coordinates.getCoordinate(
            faf,
            Headings.add(runwayCourse, asideHeading),
            calculateTurnRadius(expectedSpeed) * 2);
    return ret;
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

    IList<ApproachEntry> entries = new EList<>();
    // build approach entry
    IReadOnlyList<IafRoute> iafRoutes = context.airport.iafMappings.get(iafMapping);
    for (IafRoute iafRoute : iafRoutes) {
      ApproachEntry entry = ApproachEntry.createIaf(iafRoute).withTag("iaf via " + iafRoute.getNavaid());
      entries.add(entry);
    }

    // direct FAF entry
    {
      ICondition entryCondition = createApproachEntryConditionForThresholdAndSlope(
              context.threshold.coordinate, context.airport.altitude, radial, initialAltitude, slope);
      ApproachEntry entry = ApproachEntry.create(entryCondition).withTag("direct");
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
                PlaneShaCondition.create(
                        PlaneShaCondition.eType.altitude,
                        null, IntegerPerCategoryValue.create(daA, daB, daC, daD)),
                RunwayThresholdVisibleCondition.create()
        );
        ISet<ApproachErrorCondition> errs = ApproachErrorCondition.createSet(
                PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, IntegerPerCategoryValue.create(daA, daB, daC, daD)),
                GoingAroundNotification.GoAroundReason.decisionPointRunwayNotInSight,
                createAltitudeDifferenceRestriction(context.airport.altitude + 1800, 300, 500),
                GoingAroundNotification.GoAroundReason.unstabilizedAltitude,
                createNotStabilizedApproachErrorCondition(radial, context.airport.altitude + 1000, 16),
                GoingAroundNotification.GoAroundReason.unstabilizedHeading
        );
        stages.add(ApproachStage.create(
                type + " final",
                FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.declination, context.airport.altitude, slope),
                exitCondition,
                errs));
      }
      {
        // landing stage
        stages.add(ApproachStage.create(
                type + " landing",
                LandingBehavior.create(),
                new NeverCondition()));
      }

      Approach tmp = new Approach(type, initialAltitude, entries, beforeStagesCommands, stages, gaRoute);
      ret.add(tmp);
    }

    return ret;
  }

  private ICondition createAltitudeDifferenceRestriction(int checkedWhenBelowAltitude, Integer belowMaxDiff, Integer aboveMaxDiff) {
    ICondition ret = AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
            PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, checkedWhenBelowAltitude),
            PlaneOrderedAltitudeDifferenceCondition.create(belowMaxDiff, aboveMaxDiff));
    return ret;
  }

  private ICondition createNotStabilizedApproachErrorCondition(int radial, int checkedWhenBelowAltitude, int maxHeadingDeviance) {
    ICondition ret = AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
            PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, checkedWhenBelowAltitude),
            PlaneShaCondition.create(PlaneShaCondition.eType.heading,
                    (int) Headings.add(radial, +maxHeadingDeviance),
                    (int) Headings.add(radial, -maxHeadingDeviance)
            )
    );
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
      ApproachEntry entry = ApproachEntry.createIaf(iafRoute);
      entries.add(entry);
    }

    // entry via faf before faf
    {
      ICondition entryCondition = createApproachEntryConditionForFafByFafName(fafName, radial);
      ApproachEntry entry = ApproachEntry.create(entryCondition);
      entries.add(entry);
    }

    // entry before mapt to faf
    {
      ICondition entryCondition = createApproachEntryConditionBetweenMaptAndFaf(maptName, fafName);
      ApproachEntry entry = ApproachEntry.create(entryCondition);
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
              FixRelatedLocation.create(mapt.getCoordinate(), 0.5),
              PlaneShaCondition.create(PlaneShaCondition.eType.altitude,
                      IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD),
                      IntegerPerCategoryValue.create(mdaA + 500, mdaB + 500, mdaC + 500, mdaD + 500)),
              RunwayThresholdVisibleCondition.create()
      );
      ISet<ApproachErrorCondition> errs = ApproachErrorCondition.createSet(
              PlaneShaCondition.create(PlaneShaCondition.eType.altitude, null, IntegerPerCategoryValue.create(mdaA, mdaB, mdaC, mdaD)), GoingAroundNotification.GoAroundReason.decisionPointRunwayNotInSight,
              createNotStabilizedApproachErrorCondition(radial, context.airport.altitude + 500, 16), GoingAroundNotification.GoAroundReason.unstabilizedHeading
      );

      double slope =
              (initialAltitude - context.airport.altitude) / Coordinates.getDistanceInNM(faf.getCoordinate(), context.threshold.coordinate);
      stages.add(ApproachStage.create(
              approachType + " final",
              FlyRadialWithDescentBehavior.create(context.threshold.coordinate, radial, context.airport.declination, context.airport.altitude, slope),
              exitCondition,
              errs));
    }
    {
      // landing stage
      stages.add(ApproachStage.create(
              approachType + " landing",
              LandingBehavior.create(),
              new NeverCondition()));
    }

    Approach ret = new Approach(approachType, initialAltitude, entries, beforeStagesCommands, stages, gaRoute);

    return EList.of(ret);
  }
}
