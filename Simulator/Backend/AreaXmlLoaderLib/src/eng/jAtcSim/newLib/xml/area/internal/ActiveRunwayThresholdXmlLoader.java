package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.area.approaches.ApproachStage;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialWithDescentBehavior;
import eng.jAtcSim.newLib.area.approaches.behaviors.LandingBehavior;
import eng.jAtcSim.newLib.area.approaches.conditions.AggregatingCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.NegationCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.PlaneShaCondition;
import eng.jAtcSim.newLib.area.approaches.conditions.RunwayThresholdVisibilityCondition;
import eng.jAtcSim.newLib.area.approaches.locations.FixRelatedLocation;
import eng.jAtcSim.newLib.area.approaches.locations.ILocation;
import eng.jAtcSim.newLib.area.approaches.locations.RegionalLocation;
import eng.jAtcSim.newLib.area.approaches.perCategoryValues.IntegerPerCategoryValue;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.area.routes.IafRoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeHeadingCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;
import eng.jAtcSim.newLib.xml.area.internal.approaches.ApproachXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class ActiveRunwayThresholdXmlLoader extends XmlLoader<ActiveRunwayThreshold> {

  ActiveRunwayThresholdXmlLoader(Context context) {
    super(context);
  }

  private Approach generateDefaultVisualApproach() {

    final double VISUAL_DISTANCE = 8;
    final double VFAF_DISTANCE = 2.5;
    final int FINAL_ALTITUDE =
        (context.airport.altitude / 1000 + 2) * 1000;
    final int BASE_ALTITUDE =
        (context.airport.altitude / 1000 + 3) * 1000;
    String thrsName = context.airport.icao + ":" + context.threshold.name;


    IList<ICommand> gaCommands = new EList<>();
    gaCommands.add(ChangeHeadingCommand.createContinueCurrentHeading());
    gaCommands.add(ChangeAltitudeCommand.createClimb(BASE_ALTITUDE));
    GaRoute gaRoute = GaRoute.create(gaCommands);
    Approach ret;

    Navaid vfaf = context.area.navaids.getOrGenerate(
        thrsName + "_VFAF",
        Coordinates.getCoordinate(
            context.threshold.coordinate,
            context.threshold.getOppositeCourse(),
            VFAF_DISTANCE));
    Navaid vlft = context.area.navaids.getOrGenerate(
        thrsName + "_VRGT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(context.threshold.course, 90),
            VFAF_DISTANCE));
    Navaid vrgt = context.area.navaids.getOrGenerate(
        thrsName + "_VLFT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(context.threshold.course, -90),
            VFAF_DISTANCE));

    Coordinate a = Coordinates.getCoordinate(
        vfaf.getCoordinate(), context.threshold.course, VFAF_DISTANCE + VISUAL_DISTANCE);
    Coordinate rb = Coordinates.getCoordinate(
        a, Headings.add(context.threshold.course, 90), VISUAL_DISTANCE);
    Coordinate rc = Coordinates.getCoordinate(vfaf.getCoordinate(), Headings.add(context.threshold.course, 90), VISUAL_DISTANCE);
    Coordinate lb = Coordinates.getCoordinate(
        a, Headings.add(context.threshold.course, -90), VISUAL_DISTANCE);
    Coordinate lc = Coordinates.getCoordinate(
        vfaf.getCoordinate(), Headings.add(context.threshold.course, -90), VISUAL_DISTANCE);

    IList<ApproachEntry> entries = new EList<>();

    // direct approach
    {
      ILocation entryLocation = FixRelatedLocation.create(vfaf.getCoordinate(),
          (int) Headings.add(context.threshold.course, 90), (int) Headings.add(context.threshold.course, -90),
          VISUAL_DISTANCE);
      IafRoute route = IafRoute.create(
          EList.of(
              ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
              ProceedDirectCommand.create(vfaf.getName())),
          vfaf, PlaneCategoryDefinitions.getAll());
      ApproachEntry entry = ApproachEntry.create(entryLocation, route);
      entries.add(entry);
    }

    // right pattern approach
    {
      ILocation entryLocation = RegionalLocation.create(a, rb, rc, vfaf.getCoordinate());
      IafRoute route = IafRoute.create(
          EList.of(
              ChangeAltitudeCommand.createDescend(BASE_ALTITUDE),
              ProceedDirectCommand.create(vrgt.getName()),
              ThenCommand.create(),
              ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
              ProceedDirectCommand.create(vfaf.getName())
          ),
          vrgt, PlaneCategoryDefinitions.getAll());
      ApproachEntry entry = ApproachEntry.create(entryLocation, route);
      entries.add(entry);
    }

    // left pattern approach
    {
      ILocation entryLocation = RegionalLocation.create(a, lb, lc, vfaf.getCoordinate());
      IafRoute route = IafRoute.create(
          EList.of(
              ChangeAltitudeCommand.createDescend(BASE_ALTITUDE),
              ProceedDirectCommand.create(vlft.getName()),
              ThenCommand.create(),
              ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
              ProceedDirectCommand.create(vfaf.getName())
          ),
          vlft, PlaneCategoryDefinitions.getAll());
      ApproachEntry entry = ApproachEntry.create(entryLocation, route);
      entries.add(entry);
    }

    IList<ApproachStage> stages = new EList<>();
    // stages
    {
      ApproachStage stage = ApproachStage.create(
          FlyRadialWithDescentBehavior.create(
              context.threshold.coordinate,
              context.threshold.course,
              context.airport.altitude,
              ApproachXmlLoader.convertGlidePathDegreesToSlope(3)
          ),
          AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
              RunwayThresholdVisibilityCondition.create(),
              PlaneShaCondition.create(IntegerPerCategoryValue.create(
                  context.airport.altitude + 200,
                  context.airport.altitude + 300,
                  context.airport.altitude + 500,
                  context.airport.altitude + 500), null, null, null, null, null)
              ),
          NegationCondition.create(
              RunwayThresholdVisibilityCondition.create()),
          "Visual descent stage " + context.airport.icao + ":" + context.threshold.name);
      stages.add(stage);
      stage = ApproachStage.create(
          LandingBehavior.create(),
          null,
          NegationCondition.create(
              RunwayThresholdVisibilityCondition.create())
      );
      stages.add(stage);
    }

    ret = Approach.create(ApproachType.visual, entries, null, stages, gaRoute);
    return ret;
  }

  //TODO solve this
  @Override
  public ActiveRunwayThreshold load(XElement source) {
    throw new UnsupportedOperationException("This method is here not supported. Use 'loadBoth()' instead.");
  }

  private ActiveRunwayThreshold.Prototype loadOne(XElement source) {
    log(3, "Xml-loading active runway");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    log(3, "... active runway '%s'", name);
    Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");
    int initialDepartureAltitude = SmartXmlLoaderUtils.loadInteger("initialDepartureAltitude");
    int accelerationAltitude = SmartXmlLoaderUtils.loadInteger("accelerationAltitude",
        context.airport.altitude + 1500); // default acc-alt is 1500 ft above airport
    String mapping = SmartXmlLoaderUtils.loadString("mapping");

    context.threshold.coordinate = coordinate;
    context.threshold.name = name;

    IList<DARoute> routes = context.airport.daMappings.get(mapping);

    IList<IList<Approach>> approachesList = SmartXmlLoaderUtils.loadList(
        source.getChild("approaches").getChildren(),
        new ApproachXmlLoader(context));

    IList<Approach> approaches = approachesList.selectMany(q -> q);

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
        name, coordinate, initialDepartureAltitude, accelerationAltitude,
        approaches, routes);
    return ret;
  }

  IList<ActiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold.Prototype ia = loadOne(sources.get(0));
    ActiveRunwayThreshold.Prototype ib = loadOne(sources.get(1));

    if (ia.approaches.isAny(q -> q.getType() == ApproachType.visual)) {
      context.threshold.name = ia.name;
      context.threshold.coordinate = ia.coordinate;
      context.threshold.course = (int) Coordinates.getBearing(ia.coordinate, ib.coordinate);
      Approach visual = generateDefaultVisualApproach();
      ia.approaches.add(visual);
    }
    if (ib.approaches.isAny(q -> q.getType() == ApproachType.visual)) {
      context.threshold.name = ib.name;
      context.threshold.coordinate = ib.coordinate;
      context.threshold.course = (int) Coordinates.getBearing(ib.coordinate, ia.coordinate);
      Approach visual = generateDefaultVisualApproach();
      ib.approaches.add(visual);
    }

    IList<ActiveRunwayThreshold> ret = ActiveRunwayThreshold.create(ia, ib);
    return ret;
  }
}
