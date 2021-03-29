package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.approaches.ApproachXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class ActiveRunwayThresholdXmlLoader extends XmlLoader<ActiveRunwayThreshold> {

  private static final int DEFAULT_VISUAL_APPROACH_PATTERN_AGL_HEIGHT = 2000;

  ActiveRunwayThresholdXmlLoader(LoadingContext context) {
    super(context);
  }

  //TODO solve this
  @Override
  public ActiveRunwayThreshold load(XElement source) {
    throw new UnsupportedOperationException("This method is here not supported. Use 'loadBoth()' instead.");
  }

//  private Approach generateDefaultVisualApproach() {
//
//    final double VISUAL_DISTANCE = 8;
//    final double VFAF_DISTANCE = 2.5;
//    final int FINAL_ALTITUDE =
//            (context.airport.altitude / 1000 + 2) * 1000;
//    final int BASE_ALTITUDE =
//            (context.airport.altitude / 1000 + 3) * 1000;
//    String thrsName = context.airport.icao + ":" + context.threshold.name;
//
//
//    IList<ICommand> gaCommands = new EList<>();
//    gaCommands.add(ChangeHeadingCommand.createContinueCurrentHeading());
//    gaCommands.add(ChangeAltitudeCommand.createClimb(BASE_ALTITUDE));
//    GaRoute gaRoute = GaRoute.create(gaCommands);
//    Approach ret;
//
//    Navaid vfaf = context.area.navaids.getOrGenerate(
//            thrsName + "_VFAF",
//            Coordinates.getCoordinate(
//                    context.threshold.coordinate,
//                    context.threshold.getOppositeCourse(),
//                    VFAF_DISTANCE));
//    Navaid vlft = context.area.navaids.getOrGenerate(
//            thrsName + "_VRGT",
//            Coordinates.getCoordinate(vfaf.getCoordinate(),
//                    Headings.add(context.threshold.course, 90),
//                    VFAF_DISTANCE));
//    Navaid vrgt = context.area.navaids.getOrGenerate(
//            thrsName + "_VLFT",
//            Coordinates.getCoordinate(vfaf.getCoordinate(),
//                    Headings.add(context.threshold.course, -90),
//                    VFAF_DISTANCE));
//
//    Coordinate a = Coordinates.getCoordinate(
//            vfaf.getCoordinate(), context.threshold.course, VFAF_DISTANCE + VISUAL_DISTANCE);
//    Coordinate rb = Coordinates.getCoordinate(
//            a, Headings.add(context.threshold.course, 90), VISUAL_DISTANCE);
//    Coordinate rc = Coordinates.getCoordinate(vfaf.getCoordinate(), Headings.add(context.threshold.course, 90), VISUAL_DISTANCE);
//    Coordinate lb = Coordinates.getCoordinate(
//            a, Headings.add(context.threshold.course, -90), VISUAL_DISTANCE);
//    Coordinate lc = Coordinates.getCoordinate(
//            vfaf.getCoordinate(), Headings.add(context.threshold.course, -90), VISUAL_DISTANCE);
//
//    IList<ApproachEntry> entries = new EList<>();
//
//    // direct approach
//    {
//      ILocation entryLocation = FixRelatedLocation.create(vfaf.getCoordinate(),
//              (int) Headings.add(context.threshold.course, 90), (int) Headings.add(context.threshold.course, -90),
//              VISUAL_DISTANCE);
//      IafRoute route = IafRoute.create(
//              EList.of(
//                      ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
//                      ProceedDirectCommand.create(vfaf.getName())),
//              vfaf, PlaneCategoryDefinitions.getAll());
//      ApproachEntry entry = ApproachEntry.createIaf(entryLocation, route);
//      entries.add(entry);
//    }
//
//    // right pattern approach
//    {
//      ILocation entryLocation = RegionalLocation.create(a, rb, rc, vfaf.getCoordinate());
//      IafRoute route = IafRoute.create(
//              EList.of(
//                      ChangeAltitudeCommand.createDescend(BASE_ALTITUDE),
//                      ProceedDirectCommand.create(vrgt.getName()),
//                      ThenCommand.create(),
//                      ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
//                      ProceedDirectCommand.create(vfaf.getName())
//              ),
//              vrgt, PlaneCategoryDefinitions.getAll());
//      ApproachEntry entry = ApproachEntry.create(entryLocation, route);
//      entries.add(entry);
//    }
//
//    // left pattern approach
//    {
//      ILocation entryLocation = RegionalLocation.create(a, lb, lc, vfaf.getCoordinate());
//      IafRoute route = IafRoute.create(
//              EList.of(
//                      ChangeAltitudeCommand.createDescend(BASE_ALTITUDE),
//                      ProceedDirectCommand.create(vlft.getName()),
//                      ThenCommand.create(),
//                      ChangeAltitudeCommand.createDescend(FINAL_ALTITUDE),
//                      ProceedDirectCommand.create(vfaf.getName())
//              ),
//              vlft, PlaneCategoryDefinitions.getAll());
//      ApproachEntry entry = ApproachEntry.create(entryLocation, route);
//      entries.add(entry);
//    }
//
//    IList<ApproachStage> stages = new EList<>();
//    // stages
//    {
//      ApproachStage stage = ApproachStage.create(
//              FlyRadialWithDescentBehavior.create(
//                      context.threshold.coordinate,
//                      context.threshold.course,
//                      context.airport.declination,
//                      context.airport.altitude,
//                      ApproachXmlLoader.convertGlidePathDegreesToSlope(3)
//              ),
//              AggregatingCondition.create(AggregatingCondition.eConditionAggregator.and,
//                      RunwayThresholdVisibleCondition.create(),
//                      PlaneShaCondition.create(PlaneShaCondition.eType.altitude,
//                              IntegerPerCategoryValue.create(
//                                      context.airport.altitude + 200,
//                                      context.airport.altitude + 300,
//                                      context.airport.altitude + 500,
//                                      context.airport.altitude + 500), null)
//              ),
//              NegationCondition.create(
//                      RunwayThresholdVisibleCondition.create()),
//              "Visual descent stage " + context.airport.icao + ":" + context.threshold.name);
//      stages.add(stage);
//      stage = ApproachStage.create(
//              LandingBehavior.create(),
//              null,
//              NegationCondition.create(
//                      RunwayThresholdVisibleCondition.create())
//      );
//      stages.add(stage);
//    }
//
//    ret = new Approach(ApproachType.visual, context.airport.altitude + DEFAULT_VISUAL_APPROACH_PATTERN_AGL_HEIGHT,  entries, null, stages, gaRoute);
//    return ret;
//  }

  private ActiveRunwayThreshold.Prototype loadOneStep1(XElement source) {
    log(3, "Xml-loading active runway threshold - step 1");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    log(3, "... threshold '%s'", name);
    Coordinate coordinate = SmartXmlLoaderUtils.loadCoordinate("coordinate");
    int initialDepartureAltitude = SmartXmlLoaderUtils.loadInteger("initialDepartureAltitude");
    int accelerationAltitude = SmartXmlLoaderUtils.loadInteger("accelerationAltitude",
            context.airport.altitude + 1500); // default acc-alt is 1500 ft above airport

    context.threshold.coordinate = coordinate;
    context.threshold.name = name;

    IList<DARoute> routes = null;
    IList<Approach> approaches = null;

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
            name, coordinate, initialDepartureAltitude, accelerationAltitude,
            approaches, routes);
    return ret;
  }

  private ActiveRunwayThreshold.Prototype loadOneStep2(XElement source, ActiveRunwayThreshold.Prototype step1Template) {
    log(3, "Xml-loading active runway threshold - step 2");
    SmartXmlLoaderUtils.setContext(source);
    log(3, "... threshold '%s'", step1Template.name);
    String mapping = SmartXmlLoaderUtils.loadString("mapping");

    context.threshold.coordinate = step1Template.coordinate;
    context.threshold.name = step1Template.name;

    IList<DARoute> routes = context.airport.daMappings.get(mapping);

    IList<IList<Approach>> approachesList = SmartXmlLoaderUtils.loadList(
            source.getChild("approaches").getChildren(),
            new ApproachXmlLoader(context)::load);

    IList<Approach> approaches = approachesList.selectMany(q -> q);

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
            step1Template.name,
            step1Template.coordinate,
            step1Template.initialDepartureAltitude,
            step1Template.accelerationAltitude,
            approaches, routes);
    return ret;
  }

  IList<ActiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold.Prototype ia = loadOneStep1(sources.get(0));
    ActiveRunwayThreshold.Prototype ib = loadOneStep1(sources.get(1));

    context.threshold.name = ia.name;
    context.threshold.coordinate = ia.coordinate;
    context.threshold.course = (int) Coordinates.getBearing(ia.coordinate, ib.coordinate);
    ia = loadOneStep2(sources.get(0), ia);

    context.threshold.name = ib.name;
    context.threshold.coordinate = ib.coordinate;
    context.threshold.course = (int) Coordinates.getBearing(ib.coordinate, ia.coordinate);
    ib = loadOneStep2(sources.get(1), ib);

    IList<ActiveRunwayThreshold> ret = ActiveRunwayThreshold.create(ia, ib);
    return ret;
  }
}
