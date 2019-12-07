package eng.jAtcSim.newLib.routes;

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
import eng.jAtcSim.newLib.speeches.atc2airplane.ToNavaidCommand;
import eng.jAtcSim.sharedLib.PlaneCategoryDefinitions;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

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

  public static DARoute load(XElement source, Airport airport) {
    DARoute ret = new DARoute();
    ret.setParent(airport);
    ret.read(source);
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

  private void evaluateMaxMrvaAltitude() {
    IReadOnlyList<Navaid> routeNavaids = getNavaids();
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(routeNavaids);
    IList<Border> mrvas = this.getParent().getParent().getBorders().where(q -> q.getType() == Border.eType.mrva);

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
    this.maxMrvaAltitude = maxMrvaAlt;
  }

  private void evaluateRouteLength() {
    IReadOnlyList<Navaid> routeNavaids = getNavaids();
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

    this.routeLength = tmp;
  }

  private IList<Navaid> getNavaids() {
    IList<Navaid> ret = this.getRouteCommands()
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaid());
    return ret;
  }

  private void normalizeRouteNavaids() {
    IList<Navaid> tmp = this.getNavaids();
    if (tmp.contains(this.mainNavaid) == false) {
      switch (type) {
        case sid:
          tmp.add(this.mainNavaid);
          break;
        case star:
        case transition:
          tmp.insert(0, this.mainNavaid);
          break;
      }
    }
  }

  private void read(XElement source) {
    XmlLoader.setContext(source);

    this.type = XmlLoader.loadEnum("type", eType.class);
    this.name = XmlLoader.loadString("name");
    String mapping = XmlLoader.loadString("mapping");
    this.category = XmlLoader.loadPlaneCategory("category", "ABCD");
    this.entryAltitude = XmlLoader.loadAltitude("entryFL", null);
    String mainFixName = XmlLoader.loadString("mainFix", null);
    this.mainNavaid = mainFixName != null ?
        this.getParent().getParent().getNavaids().get(mainFixName) :
        getMainRouteNavaidFromRouteName(name, this.getParent().getParent().getNavaids());

    super.read(source, mapping);

    this.normalizeRouteNavaids();
    this.evaluateRouteLength();
    this.evaluateMaxMrvaAltitude();
  }
}
