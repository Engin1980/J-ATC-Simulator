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
import eng.jAtcSim.lib.speaking.IFromAtc;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;
import eng.jAtcSim.lib.textProcessing.parsing.Parser;
import eng.jAtcSim.lib.textProcessing.parsing.shortBlockParser.ShortBlockParser;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.xmlModel.*;

import java.awt.geom.Line2D;

public class XmlModelBinder {

  public static class Context {
    public final Area area;
    public final Airport airport;

    public Context(Area area, Airport airport) {
      this.area = area;
      this.airport = airport;
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
    IMap<String, IList<Route>> sharedRoutesGroup = convertSharedRoutes(x, context);

    Airport ret = new Airport(
        x.icao, x.name, mainAirportNavaid, x.altitude,
        x.vfrAltitude, x.transitionAltitude, x.coveredDistance, x.declination,
        initialPosition, atcTemplates, runways, inactiveRunways,
        holds, entryExitPoints, runwayConfigurations, sharedRoutesGroup, context.area);
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
      ActiveRunway runway = convert(xmlRunway, sharedRoutesGroup, context);
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

  private static IMap<String, IList<Route>> convertSharedRoutes(XmlAirport x, Context context) {
    IMap<String, IList<Route>> ret = new EMap<>();
    for (XmlAirport.XmlSharedRoutesGroup xmlSharedRoutesGroup : x.sharedRoutesGroups) {
      IList<Route> lst = new EList<>();
      ret.set(xmlSharedRoutesGroup.groupName, lst);
      for (XmlRoute xmlRoute : xmlSharedRoutesGroup.routes) {
        Route route = convert(xmlRoute, context);
        lst.add(route);
      }
    }
    return ret;
  }

  private static ActiveRunway convert(XmlActiveRunway x, IMap<String, IList<Route>> sharedRoutesGroups,
                                      Context context) {
    assert x.thresholds.size() == 2;
    IList<ActiveRunwayThreshold> thresholds = new EList<>();
    ActiveRunway ret = new ActiveRunway(thresholds, context.airport);

    XmlActiveRunwayThreshold xa = x.thresholds.get(0);
    XmlActiveRunwayThreshold xb = x.thresholds.get(1);

    IList<Approach> aApproaches = new EList<>();
    IList<Route> aRoutes = new EList<>();
    IList<Approach> bApproaches = new EList<>();
    IList<Route> bRoutes = new EList<>();


    ActiveRunwayThreshold[] t = ActiveRunwayThreshold.create(
        xa.name, xa.coordinate, xa.initialDepartureAltitude, aApproaches, aRoutes,
        xb.name, xb.coordinate, xb.initialDepartureAltitude, bApproaches, bRoutes,
        ret);
    thresholds.add(t);

    for (XmlApproach xmlApproach : xa.approaches) {
      Approach approach = convert(xmlApproach);
      aApproaches.add(approach);
    }
    for (XmlApproach xmlApproach : xb.approaches) {
      Approach approach = convert(xmlApproach);
      bApproaches.add(approach);
    }
    buildRoutesForActiveRunwayThreshold(xa, aRoutes, sharedRoutesGroups, context);
    buildRoutesForActiveRunwayThreshold(xb, bRoutes, sharedRoutesGroups, context);

    return ret;
  }

  private static Approach convert(XmlApproach xmlApproach) {
    throw new UnsupportedOperationException("TODO tohle jsem si nechal nakonec.");
  }

  private static void buildRoutesForActiveRunwayThreshold(XmlActiveRunwayThreshold xa, IList<Route> aRoutes, IMap<String, IList<Route>> sharedRoutesGroups,
                                                          Context context) {
    for (XmlRoute xmlRoute : xa.routes) {
      Route route = convert(xmlRoute, context);
      aRoutes.add(route);
    }
    if (xa.includeRoutesGroups != null) {
      String[] groupNames = xa.includeRoutesGroups.split(";");
      for (String groupName : groupNames) {
        try {
          IList<Route> routes = sharedRoutesGroups.get(groupName);
          aRoutes.add(routes);
        } catch (Exception ex) {
          throw new EApplicationException("Unable to find route group named " + groupName + " required for runway threshold " + xa.name);
        }
      }
    }
  }

  private static Route convert(XmlRoute x, Context context) {
    SpeechList routeCommands = decodeRouteCommands(x);
    IList<Navaid> routeNavaids = routeCommands
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaid());
    Navaid mainNavaid = evaluateMainRouteNavaid(
        x.name, x.type, x.mainFix, routeNavaids, context);

    double routeLength = calculateRouteLength(routeNavaids);
    int maxMrvaFL = calculateMaxMrvaFL(mainNavaid, routeNavaids, context);
    Route ret = new Route(x.type, x.name, x.category, mainNavaid, routeLength, x.entryFL, routeCommands,
        routeNavaids, maxMrvaFL, context.airport);
    return ret;
  }

  private static Navaid evaluateMainRouteNavaid(String routeName, Route.eType type, String customMainFixName, IList<Navaid> routeNavaids, Context context) {
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

  private static SpeechList decodeRouteCommands(XmlRoute route) {
    SpeechList ret;
    try {
      Parser p = new ShortBlockParser();
      SpeechList<IFromAtc> xlst = p.parseMulti(route.route);
      ret = xlst.convertTo();
    } catch (Exception ex) {
      throw new EBindException("Parsing fromAtc failed for route " + route.name + ". Route fromAtc contains error (see cause).", ex);
    }

    // hold at the end of SID via main point
    if (route.type == Route.eType.sid) {
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
