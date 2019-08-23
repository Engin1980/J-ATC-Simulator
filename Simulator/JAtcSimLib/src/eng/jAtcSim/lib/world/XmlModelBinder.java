package eng.jAtcSim.lib.world;

import eng.eSystem.Tuple;
import eng.eSystem.collections.*;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.atcs.AtcTemplate;
import eng.jAtcSim.lib.exceptions.EBindException;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.*;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.approaches.*;
import eng.jAtcSim.lib.world.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.entryLocations.FixRelatedApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.entryLocations.RegionalApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.stages.*;
import eng.jAtcSim.lib.world.approaches.stages.checks.CheckAirportVisibilityStage;
import eng.jAtcSim.lib.world.approaches.stages.checks.CheckPlaneLocationStage;
import eng.jAtcSim.lib.world.approaches.stages.checks.CheckPlaneShaStage;
import eng.jAtcSim.lib.world.xmlModel.*;
import eng.jAtcSim.lib.world.xmlModel.approachesOld.*;
import eng.jAtcSim.lib.world.xmlModel.approachesOld.approachStages.*;

import java.awt.geom.Line2D;

//TODO delete this class when everything is anywhere else
public class XmlModelBinder {

  public static class Context {
    public final Area area;
    public final Airport airport;
    public final ActiveRunwayThreshold threshold;
    public final EStack stack = new EStack();

    public Context(Area area, Airport airport) {
      this(area, airport, null);
    }

    public Context(Area area) {
      this(area, null, null);
    }

    public Context(Area area, Airport airport, ActiveRunwayThreshold threshold) {
      this.area = area;
      this.airport = airport;
      this.threshold = threshold;
    }
  }

  private static final int BORDER_ARC_POINT_DRAW_STEP = 10;

  public static Navaid convert(XmlNavaid x) {
    Navaid ret = new Navaid(x.name, x.type, x.coordinate);
    return ret;
  }

  public static PublishedHold convert(XmlPublishedHold x, Context context) {
    Navaid navaid = context.area.getNavaids().get(x.navaidName);
    PublishedHold ret = new PublishedHold(navaid, x.inboundRadial, x.turn.equals("left"), context.airport);
    return ret;
  }

  public static Border convert(XmlBorder x) {
    if (x.points.size() > 1 && x.points.isAny(q -> q instanceof XmlBorderCirclePoint)) {
      throw new EApplicationException("Border " + x.getName() + " is not valid. If <circle> is used, it must be the only element in the <points> list.");
    }
    IList<BorderPoint> points = expandArcsToPoints(x);

    Coordinate labelCoordinate = x.labelCoordinate != null
        ? x.labelCoordinate
        : generateBorderLabelCoordinate(points);

    Border ret = new Border(x.name, x.type,
        points, x.enclosed,
        x.minAltitude, x.maxAltitude,
        labelCoordinate);
    return ret;


  }

  public static Area convert(XmlArea area) {
    IList<Airport> airports = new EList<>();
    IList<Border> borders = new EList<>();
    NavaidList navaids = new NavaidList();
    Area ret = new Area(area.icao, airports, navaids, borders);

    Context context = new Context(ret, null);

    for (XmlNavaid xmlNavaid : area.navaids) {
      Navaid navaid = XmlModelBinder.convert(xmlNavaid);
      navaids.add(navaid);
    }

    area.borders.sort(new XmlBorder.ByDisjointsComparator());
    for (XmlBorder xmlBorder : area.borders) {
      Border border = XmlModelBinder.convert(xmlBorder);
      borders.add(border);
    }

    for (XmlAirport xmlAirport : area.airports) {
      Airport airport = XmlModelBinder.convert(xmlAirport, context);
      airports.add(airport);
    }

    return ret;
  }

  private static Coordinate generateBorderLabelCoordinate(IList<BorderPoint> points) {
    double latMin = points.minDouble(q -> q.getCoordinate().getLatitude().get());
    double latMax = points.maxDouble(q -> q.getCoordinate().getLatitude().get());
    double lngMin = points.minDouble(q -> q.getCoordinate().getLongitude().get());
    double lngMax = points.maxDouble(q -> q.getCoordinate().getLongitude().get());

    double lat = (latMax + latMin) / 2;
    double lng = (lngMax + lngMin) / 2;

    return new Coordinate(lat, lng);
  }

  private static Airport convert(XmlAirport x, Context context) {
    Navaid mainAirportNavaid = context.area.getNavaids().get(x.mainAirportNavaidName);
    InitialPosition initialPosition = convert(x.initialPosition);

    IList<AtcTemplate> atcTemplates = new EList<>();
    IList<ActiveRunway> runways = new EList<>();
    IList<InactiveRunway> inactiveRunways = new EList<>();
    IList<PublishedHold> holds = new EList<>();
    IList<RunwayConfiguration> runwayConfigurations = new EList<>();
    IList<EntryExitPoint> entryExitPoints = new EList<>();
    IMap<String, IList<DARoute>> sharedRoutesGroups = convertSharedRoutes(x, context);
    IMap<String, IList<IafRoute>> sharedIafRoutesGroups = convertSharedIafRoutes(x, context);

    Airport ret = new Airport(
        x.icao, x.name, mainAirportNavaid, x.altitude,
        x.vfrAltitude, x.transitionAltitude, x.coveredDistance, x.declination,
        initialPosition, atcTemplates, runways, inactiveRunways,
        holds, entryExitPoints, runwayConfigurations, sharedRoutesGroups, context.area);
    context = new Context(context.area, ret);

    for (XmlAtcTemplate xmlAtcTemplate : x.atcTemplates) {
      AtcTemplate atcTemplate = convert(xmlAtcTemplate);
      atcTemplates.add(atcTemplate);
    }

    for (XmlInactiveRunway xmlInactiveRunway : x.inactiveRunways) {
      InactiveRunway inactiveRunway = convert(xmlInactiveRunway, ret);
      inactiveRunways.add(inactiveRunway);
    }

    for (XmlActiveRunway xmlRunway : x.runways) {
      ActiveRunway runway = convert(xmlRunway, sharedRoutesGroups, sharedIafRoutesGroups, context);
      runways.add(runway);
    }

    for (XmlPublishedHold xmlPublishedHold : x.holds) {
      PublishedHold publishedHold = convert(xmlPublishedHold, context);
      holds.add(publishedHold);
    }

    for (XmlRunwayConfiguration xmlRunwayConfiguration : x.runwayConfigurations) {
      RunwayConfiguration runwayConfiguration = convert(xmlRunwayConfiguration, context);
      runwayConfigurations.add(runwayConfiguration);
    }

    for (XmlEntryExitPoint xmlEntryExitPoint : x.entryExitPoints) {
      EntryExitPoint entryExitPoint = convert(xmlEntryExitPoint, context);
      entryExitPoints.add(entryExitPoint);
    }

    return ret;
  }

  private static EntryExitPoint convert(XmlEntryExitPoint x, Context context) {
    Navaid navaid = context.area.getNavaids().get(x.name);
    Integer maxMrvaAltitude = evaluateMaxMrvaAltitudeForEntryExitPoint(x.maxMrvaAltitude, navaid, context);
    EntryExitPoint ret = new EntryExitPoint(context.airport, navaid, x.type, maxMrvaAltitude);
    return ret;
  }

  private static Integer evaluateMaxMrvaAltitudeForEntryExitPoint(Integer customMaxMrvaAltitude, Navaid navaid, Context context) {
    Integer maxMrvaAltitude;
    IList<Border> mrvas = context.area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    Tuple<Coordinate, Coordinate> line =
        new Tuple<>(
            navaid.getCoordinate(),
            context.airport.getMainAirportNavaid().getCoordinate()
        );

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (mrva.hasIntersectionWithLine(line))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }

    if (customMaxMrvaAltitude == null)
      maxMrvaAltitude = maxMrvaAlt;
    else
      maxMrvaAltitude = Math.min(customMaxMrvaAltitude, maxMrvaAlt);
    return maxMrvaAltitude;
  }

  private static RunwayConfiguration convert(XmlRunwayConfiguration x, Context context) {
    IList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals = new EList<>();
    IList<RunwayConfiguration.RunwayThresholdConfiguration> departures = new EList<>();
    IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets = new EList<>();
    RunwayConfiguration ret = new RunwayConfiguration(x.windFrom, x.windTo, x.windSpeedFrom, x.windSpeedTo,
        arrivals, departures, crossedThresholdSets);

    for (XmlRunwayConfiguration.XmlRunwayThresholdConfiguration xmlArrival : x.arrivals) {
      RunwayConfiguration.RunwayThresholdConfiguration arrival = convert(xmlArrival, context);
      arrivals.add(arrival);
    }
    for (XmlRunwayConfiguration.XmlRunwayThresholdConfiguration xmlDeparture : x.departures) {
      RunwayConfiguration.RunwayThresholdConfiguration departure = convert(xmlDeparture, context);
      departures.add(departure);
    }

    checkAllCategoriesAreApplied(departures, arrivals);
    buildCrossedThresholdsSet(crossedThresholdSets, departures, arrivals);

    return ret;
  }

  private static void buildCrossedThresholdsSet(IList<ISet<ActiveRunwayThreshold>> crossedThresholdSets,
                                                IList<RunwayConfiguration.RunwayThresholdConfiguration> departures,
                                                IList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals) {
    // detection and saving the crossed runways
    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());
    IList<ISet<ActiveRunway>> crossedRwys = new EList<>();
    while (rwys.isEmpty() == false) {
      ISet<ActiveRunway> set = new ESet<>();
      ActiveRunway r = rwys.get(0);
      rwys.removeAt(0);
      set.add(r);
      set.add(
          rwys.where(q -> isIntersectionBetweenRunways(r, q))
      );
      rwys.tryRemove(set);
      crossedRwys.add(set);
    }

    ISet<ActiveRunwayThreshold> set;
    for (ISet<ActiveRunway> crossedRwy : crossedRwys) {
      set = new ESet<>();
      for (ActiveRunway runway : crossedRwy) {
        set.add(runway.getThresholds());
      }
      crossedThresholdSets.add(set);
    }
  }

  private static boolean isIntersectionBetweenRunways(ActiveRunway a, ActiveRunway b) {
    Line2D lineA = new Line2D.Float(
        (float) a.getThresholdA().getCoordinate().getLatitude().get(),
        (float) a.getThresholdA().getCoordinate().getLongitude().get(),
        (float) a.getThresholdB().getCoordinate().getLatitude().get(),
        (float) a.getThresholdB().getCoordinate().getLongitude().get());
    Line2D lineB = new Line2D.Float(
        (float) b.getThresholdA().getCoordinate().getLatitude().get(),
        (float) b.getThresholdA().getCoordinate().getLongitude().get(),
        (float) b.getThresholdB().getCoordinate().getLatitude().get(),
        (float) b.getThresholdB().getCoordinate().getLongitude().get());
    boolean ret = lineA.intersectsLine(lineB);
    return ret;
  }

  private static void checkAllCategoriesAreApplied(
      IList<RunwayConfiguration.RunwayThresholdConfiguration> departures,
      IList<RunwayConfiguration.RunwayThresholdConfiguration> arrivals) {
    IList<ActiveRunway> rwys = new EDistinctList<>(EDistinctList.Behavior.skip);
    rwys.add(arrivals.select(q -> q.getThreshold().getParent()).distinct());
    rwys.add(departures.select(q -> q.getThreshold().getParent()).distinct());

    // check if all categories are applied
    for (char i = 'A'; i <= 'D'; i++) {
      char c = i;
      if (!arrivals.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find arrival threshold for category " + c);
      if (!departures.isAny(q -> q.isForCategory(c)))
        throw new EApplicationException("Unable to find departure threshold for category " + c);
    }
  }

  private static RunwayConfiguration.RunwayThresholdConfiguration convert(
      XmlRunwayConfiguration.XmlRunwayThresholdConfiguration x, Context context) {
    ActiveRunwayThreshold t = context.airport.getRunwayThreshold(x.name);
    RunwayConfiguration.RunwayThresholdConfiguration ret = new RunwayConfiguration.RunwayThresholdConfiguration(t);
    return ret;
  }

  private static IMap<String, IList<DARoute>> convertSharedRoutes(XmlAirport x, Context context) {
    IMap<String, IList<DARoute>> ret = new EMap<>();
    for (XmlAirport.XmlSharedRoutesGroup xmlSharedRoutesGroup : x.sharedRoutesGroups) {
      IList<DARoute> lst = new EList<>();
      ret.set(xmlSharedRoutesGroup.groupName, lst);
      for (XmlRoute xmlRoute : xmlSharedRoutesGroup.routes) {
        DARoute route = convert(xmlRoute, context);
        lst.add(route);
      }
    }
    return ret;
  }

  private static IMap<String, IList<IafRoute>> convertSharedIafRoutes(XmlAirport x, Context context) {
    IMap<String, IList<IafRoute>> ret = new EMap<>();
    for (XmlAirport.XmlSharedIafRoutesGroup xmlSharedIafRoutesGroup : x.sharedIafRoutesGroups) {
      IList<IafRoute> lst = new EList<>();
      ret.set(xmlSharedIafRoutesGroup.groupName, lst);
      for (XmlIafRoute xmlIafRoute : xmlSharedIafRoutesGroup.iafRoutes) {
        IafRoute iafRoute = convert(xmlIafRoute, context);
        lst.add(iafRoute);
      }
    }
    return ret;
  }

  private static ActiveRunway convert(XmlActiveRunway x,
                                      IMap<String, IList<DARoute>> sharedRoutesGroups,
                                      IMap<String, IList<IafRoute>> sharedIafRoutesGroups,
                                      Context context) {
    assert x.thresholds.size() == 2;
    IList<ActiveRunwayThreshold> thresholds = new EList<>();
    ActiveRunway ret = new ActiveRunway(thresholds, context.airport);

    XmlActiveRunwayThreshold xa = x.thresholds.get(0);
    XmlActiveRunwayThreshold xb = x.thresholds.get(1);

    IList<Approach> aApproaches = new EList<>();
    IList<DARoute> aRoutes = new EList<>();
    IList<Approach> bApproaches = new EList<>();
    IList<DARoute> bRoutes = new EList<>();


    ActiveRunwayThreshold[] t = ActiveRunwayThreshold.create(
        xa.name, xa.coordinate, xa.initialDepartureAltitude, aApproaches, aRoutes,
        xb.name, xb.coordinate, xb.initialDepartureAltitude, bApproaches, bRoutes,
        ret);
    thresholds.add(t);

    context = new Context(context.area, context.airport, t[0]);
    for (XmlApproach xmlApproach : xa.approaches) {
      IList<Approach> approaches = convert(xmlApproach, sharedIafRoutesGroups, context);
      aApproaches.add(approaches);
    }
    generateVisualApproachesIfRequired(aApproaches, context);
    context = new Context(context.area, context.airport, t[1]);
    for (XmlApproach xmlApproach : xb.approaches) {
      IList<Approach> approaches = convert(xmlApproach, sharedIafRoutesGroups, context);
      bApproaches.add(approaches);
    }
    generateVisualApproachesIfRequired(bApproaches, context);
    buildRoutesForActiveRunwayThreshold(xa, aRoutes, sharedRoutesGroups, context);
    buildRoutesForActiveRunwayThreshold(xb, bRoutes, sharedRoutesGroups, context);

    return ret;
  }

  private static void generateVisualApproachesIfRequired(IList<Approach> approaches, Context context) {
    // if any visual is predefined, anything is created
    if (approaches.isAny(q -> q.getType() == Approach.ApproachType.visual)) return;

    Coordinate thresholdCoordinate = context.threshold.getCoordinate();
    double thresholdCourse = context.threshold.getCourse();

    final double VISUAL_DISTANCE = 8;
    final double VFAF_DISTANCE = 2.5;
    final int FINAL_ALTITUDE =
        (context.airport.getAltitude() / 1000 + 2) * 1000;
    final int BASE_ALTITUDE =
        (context.airport.getAltitude() / 1000 + 3) * 1000;

    IApproachEntryLocation entryLocation;
    SpeechList gaCommands = new SpeechList();
    gaCommands.add(new ChangeHeadingCommand());
    gaCommands.add(new ChangeAltitudeCommand(
        ChangeAltitudeCommand.eDirection.climb, BASE_ALTITUDE));
    IList<IApproachStage> stages;
    Approach app;

    Navaid vfaf = context.area.getNavaids().getOrGenerate(
        context.threshold.getFullName() + "_VFAF",
        Coordinates.getCoordinate(
            thresholdCoordinate,
            Headings.getOpposite(thresholdCourse),
            VFAF_DISTANCE));
    Navaid vlft = context.area.getNavaids().getOrGenerate(
        context.threshold.getFullName() + "_VRGT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(thresholdCourse, 90),
            VFAF_DISTANCE));
    Navaid vrgt = context.area.getNavaids().getOrGenerate(
        context.threshold.getFullName() + "_VLFT",
        Coordinates.getCoordinate(vfaf.getCoordinate(),
            Headings.add(thresholdCourse, -90),
            VFAF_DISTANCE));

    Coordinate a = Coordinates.getCoordinate(
        vfaf.getCoordinate(), thresholdCourse, VFAF_DISTANCE + VISUAL_DISTANCE);
    Coordinate rb = Coordinates.getCoordinate(
        a, Headings.add(thresholdCourse, 90), VISUAL_DISTANCE);
    Coordinate rc = Coordinates.getCoordinate(vfaf.getCoordinate(), Headings.add(thresholdCourse, 90), VISUAL_DISTANCE);
    Coordinate lb = Coordinates.getCoordinate(
        a, Headings.add(thresholdCourse, -90), VISUAL_DISTANCE);
    Coordinate lc = Coordinates.getCoordinate(
        vfaf.getCoordinate(), Headings.add(thresholdCourse, -90), VISUAL_DISTANCE);
    Coordinate [] ecds;

    // direct approach
    entryLocation = new FixRelatedApproachEntryLocation(vfaf, VISUAL_DISTANCE,
        Headings.add(thresholdCourse, 90), Headings.add(thresholdCourse, -90));
    stages = new EList<>();
    stages.add(new RouteStage(new SpeechList<>(
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE),
        new ProceedDirectCommand(vfaf))));
    app = new Approach(Approach.ApproachType.visual, PlaneCategoryDefinitions.getAll(), gaCommands, entryLocation,
        stages, new EList<>(), context.threshold);
    approaches.add(app);

    // right pattern approach
    ecds = new Coordinate[]{};
    entryLocation = new RegionalApproachEntryLocation(
        a, rb, rc, vfaf.getCoordinate());
    stages = new EList<>();
    stages.add(new RouteStage(new SpeechList<>(
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, BASE_ALTITUDE),
        new ProceedDirectCommand(vrgt),
        new ThenCommand(),
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE),
        new ProceedDirectCommand(vfaf))));
    app = new Approach(Approach.ApproachType.visual, PlaneCategoryDefinitions.getAll(), gaCommands, entryLocation,
        stages, new EList<>(), context.threshold);
    approaches.add(app);

    // left pattern approach
    entryLocation = new RegionalApproachEntryLocation(
        a, lb, lc, vfaf.getCoordinate());
    stages = new EList<>();
    stages.add(new RouteStage(new SpeechList<>(
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, BASE_ALTITUDE),
        new ProceedDirectCommand(vlft),
        new ThenCommand(),
        new ChangeAltitudeCommand(ChangeAltitudeCommand.eDirection.descend, FINAL_ALTITUDE),
        new ProceedDirectCommand(vfaf))));
    app = new Approach(Approach.ApproachType.visual, PlaneCategoryDefinitions.getAll(), gaCommands, entryLocation,
        stages, new EList<>(), context.threshold);
    approaches.add(app);
  }

  private static IList<Approach> convert(XmlApproach xmlApproach, IMap<String, IList<IafRoute>> sharedIafRoutesGroups, Context context) {
    IList<Approach> ret = new EList<>();

    IList<XmlCustomApproach> xmlCustomApproaches;
    if (xmlApproach instanceof XmlIlsApproach)
      xmlCustomApproaches = convertFromIls((XmlIlsApproach) xmlApproach, context);
    else if (xmlApproach instanceof XmlUnpreciseApproach)
      xmlCustomApproaches = convertFromUnprecise((XmlUnpreciseApproach) xmlApproach, context);
    else if (xmlApproach instanceof XmlGnssApproach)
      xmlCustomApproaches = convertFromGnss((XmlGnssApproach) xmlApproach, context);
    else
      xmlCustomApproaches = convertFromCustom((XmlCustomApproach) xmlApproach, context);

    for (XmlCustomApproach xmlCustomApproach : xmlCustomApproaches) {
      Approach app = convertCustom(xmlCustomApproach, context, sharedIafRoutesGroups);
      ret.add(app);
    }
    return ret;
  }

  private static Approach convertCustom(XmlCustomApproach x, Context context, IMap<String, IList<IafRoute>> sharedIafRoutesGroups) {
    SpeechList<IAtcCommand> gaCommands = decodeGaCommands(x);
    IApproachEntryLocation entryLocation = convert(x.entryLocation, context);
    IList<IafRoute> iafRoutes = new EList<>();
    IList<IApproachStage> stages = new EList<>();
    Approach ret = new Approach(x.type, x.planeCategories, gaCommands, entryLocation, stages, iafRoutes, context.threshold);

    for (XmlStage xmlStage : x.stages) {
      IApproachStage stage = convert(xmlStage, context);
      stages.add(stage);
    }

    buildRoutesForApproach(x, iafRoutes, sharedIafRoutesGroups, context);
    /*
     gaCommands = parseRoute(gaRoute);
//
//    if (this.includeIafRoutesGroups != null) {
//      String[] groupNames = this.includeIafRoutesGroups.split(";");
//      for (String groupName : groupNames) {
//        Airport.SharedIafRoutesGroup group = this.getParent().getParent().getParent().getSharedIafRoutesGroups().tryGetFirst(q -> q.groupName.equals(groupName));
//        if (group == null) {
//          throw new EApplicationException("Unable to find iaf-route group named " + groupName + " in airport "
//              + this.getParent().getParent().getParent().getIcao() + " required for runway approach " + this.getParent().getName() + " " + this.getTypeString() + ".");
//        }
//
//        this.iafRoutes.add(group.iafRoutes);
//      }
//    }
//
//    this.iafRoutes.forEach(q -> q.bind());
//
//    this._bind(); // bind in descendants
//
//    this.geographicalRadial = (int) Math.round(
//        Headings.add(this.radial,
//            this.getParent().getParent().getParent().getDeclination()));
     */
    return ret;
  }

  private static IApproachStage convert(XmlStage x, Context context) {
    IApproachStage ret;

    if (x instanceof XmlCheckAirportVisibilityStage)
      ret = new CheckAirportVisibilityStage();
    else if (x instanceof XmlLandingStage)
      ret = new LandingStage(context.threshold);
    else if (x instanceof XmlCheckPlaneLocationStage)
      ret = convertCheckPlaneLocationStage((XmlCheckPlaneLocationStage) x, context);
    else if (x instanceof XmlCheckPlaneStateStage)
      ret = convertCheckPlaneStateStage((XmlCheckPlaneStateStage) x, context);
    else if (x instanceof XmlRouteStage)
      ret = convertRouteStage((XmlRouteStage) x, context);
    else if (x instanceof XmlDescendStage)
      ret = convertDescendStage((XmlDescendStage) x, context);
    else
      throw new UnsupportedOperationException();

    return ret;
  }

  private static DescendStage convertDescendStage(XmlDescendStage x, Context context) {
    Navaid fix = context.area.getNavaids().get(x.fix);
    Navaid exitFix;
    if (x.exitFix != null)
      exitFix = context.area.getNavaids().get(x.exitFix);
    else
      exitFix = null;
    DescendStage ret = new DescendStage(fix, x.altitude, x.slope, exitFix, x.exitAltitude);
    return ret;
  }

  private static RouteStage convertRouteStage(XmlRouteStage x, Context context) {
    SpeechList route = decodeRouteFromString(x.route);
    RouteStage ret = new RouteStage(route);
    return ret;
  }

  private static CheckPlaneShaStage convertCheckPlaneStateStage(XmlCheckPlaneStateStage x, Context context) {
    CheckPlaneShaStage ret = new CheckPlaneShaStage(
        x.minAltitude, x.maxAltitude, x.minHeading, x.maxHeading, x.minSpeed, x.maxSpeed
    );
    return ret;
  }

  private static CheckPlaneLocationStage convertCheckPlaneLocationStage(XmlCheckPlaneLocationStage x, Context context) {
    Coordinate c;
    if (x.coordinate != null)
      c = x.coordinate;
    else
      c = context.area.getNavaids().get(x.fix).getCoordinate();
    CheckPlaneLocationStage ret = new CheckPlaneLocationStage(
        c, x.minDistance, x.maxDistance, x.minHeading, x.maxHeading
    );
    return ret;
  }

  private static IApproachEntryLocation convert(XmlApproachEntryLocation x, Context context) {
    IApproachEntryLocation ret;
    if (x instanceof XmlFixRelatedApproachEntryLocation) {
      XmlFixRelatedApproachEntryLocation fx = (XmlFixRelatedApproachEntryLocation) x;
      Navaid n = context.area.getNavaids().get(fx.navaid);
      ret = new FixRelatedApproachEntryLocation(n, fx.maximalDistance, fx.fromRadial, fx.toRadial);
    } else {
      XmlRegionalApproachEntryLocation rx = (XmlRegionalApproachEntryLocation) x;
      ret = new RegionalApproachEntryLocation(rx.points);
    }
    return ret;
  }

  private static void buildRoutesForApproach(XmlCustomApproach x, IList<IafRoute> approachRoutes, IMap<String, IList<IafRoute>> sharedIafRoutesGroups, Context context) {

    for (XmlIafRoute xmlRoute : x.iafRoutes) {
      IafRoute route = convert(xmlRoute, context);
      approachRoutes.add(route);
    }
    if (x.includeIafRoutesGroups != null) {
      String[] groupNames = x.includeIafRoutesGroups.split(";");
      for (String groupName : groupNames) {
        try {
          IList<IafRoute> routes = sharedIafRoutesGroups.get(groupName);
          approachRoutes.add(routes);
        } catch (Exception ex) {
          throw new EApplicationException("Unable to find route group named " + groupName + ".");
        }
      }
    }
  }

  private static IafRoute convert(XmlIafRoute x, Context context) {
    Navaid navaid = context.area.getNavaids().get(x.iaf);
    SpeechList routeCommands = decodeRouteFromString(x.route);
    IafRoute ret = new IafRoute(navaid, routeCommands, x.category);
    return ret;
  }

  private static SpeechList<IAtcCommand> decodeGaCommands(XmlCustomApproach x) {
    SpeechList ret;
    try {
      ret = decodeRouteFromString(x.gaRoute);
    } catch (Exception ex) {
      throw new EApplicationException("Unable to decode go-around routing for approach.");
    }
    return ret;
  }

  private static IList<XmlCustomApproach> convertFromCustom(XmlCustomApproach x, Context context) {
    IList<XmlCustomApproach> ret = new EList<>();
    for (char planeCategory : x.categories.toUpperCase().toCharArray()) {

      XmlCustomApproach app = new XmlCustomApproach();
      app.type = x.type;
      app.categories = Character.toString(planeCategory);
      for (XmlStage stage : x.stages) {
        app.stages.add(stage);
      }

      ret.add(app);
    }
    return ret;
  }

  private static IList<XmlCustomApproach> convertFromIls(XmlIlsApproach x, Context context) {
    IList<XmlCustomApproach> ret = new EList<>();
    for (char planeCategory : x.categories.toUpperCase().toCharArray()) {

      for (XmlIlsApproach.Category ilsCategory : x.ilsCategories) {

        XmlCustomApproach app = new XmlCustomApproach();
        app.categories = Character.toString(planeCategory);
        app.type = ilsCategory.kind == XmlIlsApproach.Kind.I
            ? Approach.ApproachType.ils_I
            : ilsCategory.kind == XmlIlsApproach.Kind.II
            ? Approach.ApproachType.ils_II
            : Approach.ApproachType.ils_III;
        app.stages.add(
            new XmlCheckPlaneLocationStage(context.threshold.getCoordinate(),
                Headings.add(x.radial, -20),
                Headings.add(x.radial, 20),
                5, 15));

        int minAlt = x.minimalInitialAltitude == null ? x.initialAltitude : x.minimalInitialAltitude;
        app.stages.add(
            XmlCheckPlaneStateStage.createAltitude(minAlt, x.initialAltitude + 1000));

        int da = ilsCategory.getDA(planeCategory);
        app.stages.add(
            new XmlDescendStage(context.airport.getMainAirportNavaid().getName(), context.airport.getAltitude(), x.slope,
                null, da));
        app.stages.add(new XmlCheckAirportVisibilityStage());
        app.stages.add(new XmlLandingStage());

        ret.add(app);
      }
    }
    return ret;
  }

  private static IList<XmlCustomApproach> convertFromGnss(XmlGnssApproach x, Context context) {
    IList<XmlCustomApproach> ret = new EList<>();
    for (char planeCategory : x.categories.toUpperCase().toCharArray()) {

      XmlCustomApproach app = new XmlCustomApproach();
      app.categories = Character.toString(planeCategory);
      app.type = Approach.ApproachType.gnss;
      String tmp = context.airport.getMainAirportNavaid().getName();
      app.stages.add(
          new XmlCheckPlaneLocationStage(tmp,
              Headings.add(x.radial, -20),
              Headings.add(x.radial, 20),
              5, 15));

      app.stages.add(
          XmlCheckPlaneStateStage.createAltitude(x.initialAltitude, x.initialAltitude + 1000));

      int da = x.getDA(planeCategory);
      app.stages.add(
          new XmlDescendStage(tmp, x.initialAltitude, x.slope,
              null, da));
      app.stages.add(new XmlCheckAirportVisibilityStage());
      app.stages.add(new XmlLandingStage());

      ret.add(app);
    }
    return ret;
  }

  private static IList<XmlCustomApproach> convertFromUnprecise(XmlUnpreciseApproach x, Context context) {
    IList<XmlCustomApproach> ret = new EList<>();
    for (char planeCategory : x.categories.toUpperCase().toCharArray()) {

      XmlCustomApproach app = new XmlCustomApproach();
      app.categories = Character.toString(planeCategory);
      app.type = x.kind == XmlUnpreciseApproach.Kind.vor
          ? Approach.ApproachType.vor
          : Approach.ApproachType.ndb;
      String tmp = context.airport.getMainAirportNavaid().getName();
      app.stages.add(
          new XmlCheckPlaneLocationStage(x.faf,
              Headings.add(x.radial, -45),
              Headings.add(x.radial, 45),
              1, 10));

      app.stages.add(
          XmlCheckPlaneStateStage.createAltitude(x.initialAltitude, x.initialAltitude + 1000));

      int da = x.getMDA(planeCategory);
      app.stages.add(
          new XmlDescendStage(tmp, x.initialAltitude, x.slope,
              null, da));
      app.stages.add(new XmlCheckAirportVisibilityStage());
      app.stages.add(new XmlLandingStage());

      ret.add(app);
    }
    return ret;
  }


  private static void buildRoutesForActiveRunwayThreshold(XmlActiveRunwayThreshold xa, IList<DARoute> aRoutes, IMap<String, IList<DARoute>> sharedRoutesGroups,
                                                          Context context) {
    for (XmlRoute xmlRoute : xa.routes) {
      DARoute route = convert(xmlRoute, context);
      aRoutes.add(route);
    }
    if (xa.includeRoutesGroups != null) {
      String[] groupNames = xa.includeRoutesGroups.split(";");
      for (String groupName : groupNames) {
        try {
          IList<DARoute> routes = sharedRoutesGroups.get(groupName);
          aRoutes.add(routes);
        } catch (Exception ex) {
          throw new EApplicationException("Unable to find route group named " + groupName + " required for runway threshold " + xa.name);
        }
      }
    }
  }

  private static DARoute convert(XmlRoute x, Context context) {
    SpeechList routeCommands = decodeRouteCommands(x);
    IList<Navaid> routeNavaids = routeCommands
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaid());
    Navaid mainNavaid = evaluateMainRouteNavaid(
        x.name, x.type, x.mainFix, routeNavaids, context);

    double routeLength = calculateRouteLength(routeNavaids);
    int maxMrvaFL = calculateMaxMrvaFL(mainNavaid, routeNavaids, context);
    DARoute ret = new DARoute(x.type, x.name, x.category, mainNavaid, routeLength, x.entryFL, routeCommands,
        routeNavaids, maxMrvaFL, context.airport);
    return ret;
  }

  private static Navaid evaluateMainRouteNavaid(String routeName, DARoute.eType type, String customMainFixName, IList<Navaid> routeNavaids, Context context) {
    Navaid customMainFix = customMainFixName != null && !customMainFixName.isEmpty()
        ? context.area.getNavaids().get(customMainFixName)
        : null;
    Navaid ret;

    switch (type) {
      case sid:
        ret = customMainFix == null ? getFixByRouteName(routeName, context) : customMainFix;
        if (!routeNavaids.isEmpty() && !routeNavaids.getLast().equals(ret))
          routeNavaids.add(ret);
        break;
      case star:
      case transition:
        ret = customMainFix == null ? getFixByRouteName(routeName, context) : customMainFix;
        if (!routeNavaids.isEmpty() && !routeNavaids.getFirst().equals(ret))
          routeNavaids.insert(0, ret);
        break;
      case vectoring:
        // nothing
        ret = null;
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }
    return ret;
  }

  private static Navaid getFixByRouteName(String routeName, Context context) {
    Navaid ret;
    String name = RegexUtils.extractGroupContent(routeName, "^([A-Z]+)\\d.+", 1);
    ret = Acc.area().getNavaids().get(name);
    return ret;
  }

  private static int calculateMaxMrvaFL(Navaid mainNavaid, IList<Navaid> routeNavaids, Context context) {
    // min alt
    IList<Border> mrvas = context.area.getBorders().where(q -> q.getType() == Border.eType.mrva);
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(routeNavaids);

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (hasMrvaIntersection(pointLines, mrva))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }
    if (maxMrvaAlt == 0) {
      Navaid routePoint = mainNavaid;
      Border mrva = mrvas.tryGetFirst(q -> q.isIn(routePoint.getCoordinate()));
      if (mrva != null)
        maxMrvaAlt = mrva.getMaxAltitude();
    }
    int ret = maxMrvaAlt / 100;
    return ret;
  }

  private static boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
    return ret;
  }

  private static IList<Tuple<Coordinate, Coordinate>> convertPointsToLines(IList<Navaid> points) {
    IList<Tuple<Coordinate, Coordinate>> ret = new EList<>();

    for (int i = 1; i < points.size(); i++) {
      Navaid bef = points.get(i - 1);
      Navaid aft = points.get(i);
      ret.add(new Tuple<>(bef.getCoordinate(), aft.getCoordinate()));
    }

    return ret;
  }

  private static double calculateRouteLength(IList<Navaid> routeNavaids) {
    double ret = 0;
    Navaid prev = null;

    for (Navaid routeNavaid : routeNavaids) {
      if (prev == null) {
        prev = routeNavaid;
      } else {
        Navaid curr = routeNavaid;
        double dist = Coordinates.getDistanceInNM(prev.getCoordinate(), curr.getCoordinate());
        ret += dist;
        prev = curr;
      }
    }

    return ret;
  }

  private static SpeechList decodeRouteFromString(String route) {
    Parser p = new ShortBlockParser();
    SpeechList<IFromAtc> xlst = p.parseMulti(route);
    return xlst;
  }

  private static SpeechList decodeRouteCommands(XmlRoute route) {
    SpeechList ret;
    try {
      ret = decodeRouteFromString(route.route);
    } catch (Exception ex) {
      throw new EBindException("Parsing fromAtc failed for route " + route.name + ". Route fromAtc contains error (see cause).", ex);
    }

    // hold at the end of SID via main point
    if (route.type == DARoute.eType.sid) {
      ToNavaidCommand tnc = (ToNavaidCommand) ret.tryGetLast(q -> q instanceof ToNavaidCommand);
      assert tnc != null : "No ToNavaidCommand in SID???";
      if (tnc instanceof HoldCommand == false) {
        ret.add(new HoldCommand(tnc.getNavaid(), 270, true));
      }
    }

    return ret;
  }

  private static InactiveRunway convert(XmlInactiveRunway x, Airport parent) {
    assert x.thresholds.size() == 2;
    IList<InactiveRunwayThreshold> thresholds = new EList<>();
    InactiveRunway ret = new InactiveRunway(thresholds, parent);

    XmlInactiveRunwayThreshold xa = x.thresholds.get(0);
    XmlInactiveRunwayThreshold xb = x.thresholds.get(1);
    InactiveRunwayThreshold[] t = InactiveRunwayThreshold.create(
        xa.name, xa.coordinate,
        xb.name, xb.coordinate,
        ret);
    thresholds.add(t);

    return ret;
  }

  private static AtcTemplate convert(XmlAtcTemplate x) {
    return new AtcTemplate(
        x.type, x.name, x.frequency, x.acceptAltitude, x.releaseAltitude, x.orderedAltitude,
        x.ctrAcceptDistance, x.ctrNavaidAcceptDistance);
  }

  private static InitialPosition convert(XmlInitialPosition initialPosition) {
    return new InitialPosition(initialPosition.coordinate, initialPosition.range);
  }

  private static BorderPoint convert(XmlBorderExactPoint xmlBorderExactPoint) {
    BorderPoint ret = new BorderPoint(xmlBorderExactPoint.getCoordinate());
    return ret;
  }

  private static IList<BorderPoint> expandArcsToPoints(XmlBorder x) {
    IList<XmlBorderPoint> points = x.points;
    IList<XmlBorderExactPoint> exp = new EList<>();
    IList<BorderPoint> ret = new EList<>();

    // expand circle
    if (points.size() > 0 && points.get(0) instanceof XmlBorderCirclePoint) {
      XmlBorderCirclePoint bcp = (XmlBorderCirclePoint) points.get(0);
      x.labelCoordinate = bcp.getCoordinate();
      points.clear();
      points.add(new XmlBorderCrdPoint(bcp.getCoordinate(), 0, bcp.getDistance()));
      points.add(new XmlBorderArcPoint(bcp.getCoordinate(), XmlBorderArcPoint.eDirection.clockwise));
      points.add(new XmlBorderCrdPoint(bcp.getCoordinate(), 180, bcp.getDistance()));
      points.add(new XmlBorderArcPoint(bcp.getCoordinate(), XmlBorderArcPoint.eDirection.clockwise));
      x.enclosed = true;
    }

    if (x.isEnclosed() && !x.points.get(0).equals(x.points.get(x.points.size() - 1)))
      x.points.add(x.points.get(0));

    // replace CRD to Exact
    IList<XmlBorderPoint> lst = new EList<>(x.points);
    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof XmlBorderCrdPoint) {
        XmlBorderCrdPoint bcp = (XmlBorderCrdPoint) lst.get(i);
        Coordinate c = Coordinates.getCoordinate(bcp.getCoordinate(), bcp.getRadial(), bcp.getDistance());
        XmlBorderExactPoint bep = new XmlBorderExactPoint(c);
        lst.set(i, bep);
      }
    }

    for (int i = 0; i < lst.size(); i++) {
      if (lst.get(i) instanceof XmlBorderExactPoint)
        exp.add((XmlBorderExactPoint) lst.get(i));
      else {
        XmlBorderExactPoint prev = (XmlBorderExactPoint) lst.get(i - 1);
        XmlBorderArcPoint curr = (XmlBorderArcPoint) lst.get(i);
        XmlBorderExactPoint next = (XmlBorderExactPoint) lst.get(i + 1);
        IList<XmlBorderExactPoint> tmp = generateArcPoints(prev, curr, next);
        exp.add(tmp);
      }
    }

    for (XmlBorderExactPoint xmlBorderExactPoint : exp) {
      BorderPoint bp = XmlModelBinder.convert(xmlBorderExactPoint);
      ret.add(bp);
    }
    return ret;
  }

  private static IList<XmlBorderExactPoint> generateArcPoints(XmlBorderExactPoint prev, XmlBorderArcPoint curr, XmlBorderExactPoint next) {
    IList<XmlBorderExactPoint> ret = new EList<>();

    double prevHdg = Coordinates.getBearing(curr.getCoordinate(), prev.getCoordinate());
    double nextHdg = Coordinates.getBearing(curr.getCoordinate(), next.getCoordinate());
    double dist = Coordinates.getDistanceInNM(curr.getCoordinate(), prev.getCoordinate());
    dist = (dist + Coordinates.getDistanceInNM(curr.getCoordinate(), next.getCoordinate())) / 2;
    double step;
    if (curr.getDirection() == XmlBorderArcPoint.eDirection.clockwise) {
      prevHdg = Math.ceil(prevHdg);
      nextHdg = Math.floor(nextHdg);
      step = 1;
    } else if (curr.getDirection() == XmlBorderArcPoint.eDirection.counterclockwise) {
      prevHdg = Math.floor(prevHdg);
      nextHdg = Math.ceil(nextHdg);
      step = -+1;
    } else {
      throw new UnsupportedOperationException("This combination is not supported.");
    }
    double pt = prevHdg;
    while (pt != nextHdg) {
      pt = Headings.add(pt, step);
      if (((int) pt) % BORDER_ARC_POINT_DRAW_STEP == 0) {
        Coordinate c = Coordinates.getCoordinate(curr.getCoordinate(), pt, dist);
        XmlBorderExactPoint p = new XmlBorderExactPoint(c);
        ret.add(p);
      }
    }

    return ret;
  }
}
