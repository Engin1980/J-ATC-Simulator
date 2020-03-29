package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.shared.PlaneCategoryDefinitions;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.ToNavaidCommand;

public class DARoute extends Route {
  public enum eType {

    sid,
    star,
    transition,
    vectoring;

    public boolean isArrival() {
      return this == star || this == transition;
    }
  }

  static class XmlLoader{
    public static DARoute load(XElement source, Airport airport) {
      DARoute ret = new DARoute();
      ret.setParent(airport);
      read(source, ret);
      return ret;
    }

    private static void read(XElement source, DARoute route) {
      XmlLoaderUtils.setContext(source);

      route.type = XmlLoaderUtils.loadEnum("type", eType.class);
      route.name = XmlLoaderUtils.loadString("name");
      String mapping = XmlLoaderUtils.loadString("mapping");
      route.category = XmlLoaderUtils.loadPlaneCategory("category", "ABCD");
      route.entryAltitude = XmlLoaderUtils.loadAltitude("entryFL", null);
      String mainFixName = XmlLoaderUtils.loadString("mainFix", null);
      route.mainNavaid = mainFixName != null ?
          route.getParent().getParent().getNavaids().get(mainFixName) :
          getMainRouteNavaidFromRouteName(route.name, route.getParent().getParent().getNavaids());

      super.read(source, mapping);

      normalizeRouteNavaids(route);
      evaluateRouteLength(route);
      evaluateMaxMrvaAltitude(route);
    }

    private static void evaluateMaxMrvaAltitude(DARoute route) {
      IReadOnlyList<Navaid> routeNavaids = route.getNavaids();
      IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(routeNavaids);
      IList<Border> mrvas = route.getParent().getParent().getBorders().where(q -> q.getType() == Border.eType.mrva);

      int maxMrvaAlt = 0;
      for (Border mrva : mrvas) {
        if (hasMrvaIntersection(pointLines, mrva))
          maxMrvaAlt = Math.max(maxMrvaAlt, mrva.getMaxAltitude());
      }
      if (maxMrvaAlt == 0) {
        Navaid routePoint = route.mainNavaid;
        Border mrva = mrvas.tryGetFirst(q -> q.isIn(routePoint.getCoordinate()));
        if (mrva != null)
          maxMrvaAlt = mrva.getMaxAltitude();
      }
      route.maxMrvaAltitude = maxMrvaAlt;
    }

    private static void evaluateRouteLength(DARoute route) {
      IReadOnlyList<String> routeNavaids = route.getNavaidsNames();
      double tmp = 0;
      Navaid prev = null;

      for (Navaid routeNavaid : routeNavaids) {
        if (prev == null) {
          prev = routeNavaid;
        } else {
          Navaid curr = routeNavaid;
          double dist = Coordinates.getDistanceInNM(prev.getCoordinate(), curr.getCoordinate());
          tmp += dist;
          prev = curr;
        }
      }

      route.routeLength = tmp;
    }

    private static void normalizeRouteNavaids(DARoute route) {
//      IList<String> tmp = route.getNavaidNames();
//      if (tmp.contains(route.mainNavaid.getName()) == false) {
//        switch (route.type) {
//          case sid:
//            tmp.add(route.mainNavaid);
//            break;
//          case star:
//          case transition:
//            tmp.insert(0, route.mainNavaid);
//            break;
//        }
//      }
    }
  }

  public static DARoute createNewVectoringByFix(Navaid n) {
    DARoute ret = new DARoute();
    ret.type = eType.vectoring;
    ret.name = n.getName() + "/v";
    ret.category = PlaneCategoryDefinitions.getAll();
    ret.mainNavaid = n;
    ret.routeLength = -1;
    ret.maxMrvaAltitude = null;
    return ret;
  }

  private static Navaid getMainRouteNavaidFromRouteName(String routeName, NavaidList navaids) {
    String name = RegexUtils.extractGroupContent(routeName, "^([A-Z]+)\\d.+", 1);
    Navaid ret = navaids.get(name);
    return ret;
  }

  private static IList<Tuple<Coordinate, Coordinate>> convertPointsToLines(IReadOnlyList<Navaid> points) {
    IList<Tuple<Coordinate, Coordinate>> ret = new EList<>();

    for (int i = 1; i < points.size(); i++) {
      Navaid bef = points.get(i - 1);
      Navaid aft = points.get(i);
      ret.add(new Tuple<>(bef.getCoordinate(), aft.getCoordinate()));
    }

    return ret;
  }

  private static boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
    return ret;
  }
  private eType type;
  private String name;
  private PlaneCategoryDefinitions category;
  private double routeLength;
  private Navaid mainNavaid;
  private Integer entryAltitude;
  private Integer maxMrvaAltitude;

  private DARoute() {
  }

  public PlaneCategoryDefinitions getCategory() {
    return this.category;
  }

  public Integer getEntryAltitude() {
    return entryAltitude;
  }

  public Navaid getMainNavaid() {
    return mainNavaid;
  }

  public int getMaxMrvaAltitude() {
    int ret = maxMrvaAltitude == null ? 0 : maxMrvaAltitude * 100;
    return ret;
  }

  public String getName() {
    return name;
  }

  public double getRouteLength() {
    return routeLength;
  }

  public eType getType() {
    return type;
  }

  public boolean isValidForCategory(char categoryChar) {
    boolean ret = this.category.contains(categoryChar);
    return ret;
  }

  @Override
  public String toString() {
    return "Route{" +
        type +
        ",'" + name + "'}";
  }



  private IList<String> getNavaidNames() {
    //TODO this is strange. In XmlLoadere there is a function which extends
    // this set, but it is not used anywhere???
    IList<String> ret = this.getRouteCommands()
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaidName());
    return ret;
  }

}
