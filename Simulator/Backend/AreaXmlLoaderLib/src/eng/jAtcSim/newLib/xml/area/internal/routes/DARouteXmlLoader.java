package eng.jAtcSim.newLib.xml.area.internal.routes;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

public class DARouteXmlLoader extends XmlLoader<DARoute> {

  public DARouteXmlLoader(Context context) {
    super(context);
  }

  @Override
  public DARoute load(XElement source) {
    XmlLoaderUtils.setContext(source);

    DARouteType type = XmlLoaderUtils.loadEnum("type", DARouteType.class);
    String name = XmlLoaderUtils.loadString("name");
    String mapping = XmlLoaderUtils.loadString("mapping");
    PlaneCategoryDefinitions category = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");
    Integer entryAltitude = XmlLoaderUtils.loadAltitude("entryFL", null);
    String mainFixName = XmlLoaderUtils.loadString("mainFix", null);
    Navaid mainNavaid = mainFixName != null ?
        context.area.navaids.get(mainFixName) :
        getMainRouteNavaidFromRouteName(name);

    IList<ICommand> commands = XmlLoaderUtils.loadList(
        source.getChildren(),
        new SpeechXmlLoader()
    );

    IReadOnlyList<Navaid> routeNavaids = getNavaidsFromCommands(commands);

    double length = evaluateRouteLength(routeNavaids);
    int maxMrvaAltitude = evaluateMaxMrvaAltitude(routeNavaids, mainNavaid);

    DARoute ret = new DARoute(commands, type, name, category, length, mainNavaid, entryAltitude, maxMrvaAltitude);
    context.airport.daMappings.add(mapping, ret);

    return ret;
  }

  private IList<Tuple<Coordinate, Coordinate>> convertPointsToLines(IReadOnlyList<Navaid> points) {
    IList<Tuple<Coordinate, Coordinate>> ret = new EList<>();

    for (int i = 1; i < points.size(); i++) {
      Navaid bef = points.get(i - 1);
      Navaid aft = points.get(i);
      ret.add(new Tuple<>(bef.getCoordinate(), aft.getCoordinate()));
    }

    return ret;
  }

  private int evaluateMaxMrvaAltitude(IReadOnlyList<Navaid> routeNavaids, Navaid mainRouteNavaid) {
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(routeNavaids);
    IList<Border> mrvas = context.area.borders.where(q -> q.getType() == Border.eType.mrva);

    int maxMrvaAlt = 0;
    for (Border mrva : mrvas) {
      if (hasMrvaIntersection(pointLines, mrva))
        maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
    }
    if (maxMrvaAlt == 0) {
      Navaid routePoint = mainRouteNavaid;
      Border mrva = mrvas.tryGetFirst(q -> q.isIn(routePoint.getCoordinate()));
      if (mrva != null)
        maxMrvaAlt = mrva.getMaxAltitude();
    }
    return maxMrvaAlt;
  }

  private double evaluateRouteLength(IReadOnlyList<Navaid> routeNavaids) {
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

  private Navaid getMainRouteNavaidFromRouteName(String routeName) {
    String name = RegexUtils.extractGroupContent(routeName, "^([A-Z]+)\\d.+", 1);
    Navaid ret = context.area.navaids.get(name);
    return ret;
  }

  private IReadOnlyList<Navaid> getNavaidsFromCommands(IReadOnlyList<ICommand> commands) {
    IList<String> navaidNames = commands
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaidName());
    IList<Navaid> ret = navaidNames.select(q -> context.area.navaids.get(q));
    return ret;
  }

  private boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
    return ret;
  }

//  private void normalizeRouteNavaids(IList<ICommand> commands, IList<Navaid> routeNavaids, Navaid mainRouteNavaid, DARoute.eType type) {
//    if (routeNavaids.contains(mainRouteNavaid) == false) {
//      switch (type) {
//        case sid:
//          commands.add(route.mainNavaid);
//          break;
//        case star:
//        case transition:
//          tmp.insert(0, route.mainNavaid);
//          break;
//      }
//    }
//  }
}
