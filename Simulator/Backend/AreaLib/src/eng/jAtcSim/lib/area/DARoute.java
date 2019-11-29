/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.area;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.utilites.RegexUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.area.xml.XmlLoader;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ToNavaidCommand;

/**
 * @author Marek
 */
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
    DARoute ret = new DARoute(eType.vectoring, n.getName()+"/v",
        PlaneCategoryDefinitions.getAll(), n, -1, null, new EList<>(), null, "");

    return ret;
  }

  public static IList<DARoute> loadList(XElement source,
                                        NavaidList navaids, IReadOnlyList<PublishedHold> holds, IReadOnlyList<Border> mrvas) {
    IList<XElement> daElements = lookForElementRecursively(source, "route");

    IList<DARoute> ret = new EList<>();
    for (XElement daElement : daElements) {
      DARoute tmp = DARoute.load(daElement, navaids, holds, mrvas);
      ret.add(tmp);
    }
    return ret;
  }

  public static DARoute load(XElement source, NavaidList navaids, IReadOnlyList<PublishedHold> holds,
                             IReadOnlyList<Border> mrvas) {
    XmlLoader.setContext(source);
    DARoute.eType type = XmlLoader.loadEnum("type", DARoute.eType.class);
    String name = XmlLoader.loadString("name");
    String mapping = XmlLoader.loadString("mapping");
    PlaneCategoryDefinitions category = XmlLoader.loadPlaneCategory("category", "ABCD");
    Integer entryAltitude = XmlLoader.loadAltitude("entryFL", null);
    String mainFixName = XmlLoader.loadString("mainFix", null);
    Navaid mainNavaid = mainFixName != null ? navaids.get(mainFixName) : getMainRouteNavaidFromRouteName(name, navaids);

    IList<IAtcCommand> cmds = loadCommands(source, navaids, holds);

    IList<Navaid> routeNavaids = evaluateRouteNavaids(cmds, type, mainNavaid);
    double routeLength = evaluateRouteLength(routeNavaids);
    Integer maxMrvaAltitude = evaluateMaxMrvaAltitude(mainNavaid, routeNavaids, mrvas);
    DARoute ret = new DARoute(type, name, category, mainNavaid, routeLength, entryAltitude, cmds, maxMrvaAltitude, mapping);
    return ret;
  }

  private static Integer evaluateMaxMrvaAltitude(Navaid mainNavaid, IReadOnlyList<Navaid> routeNavaids, IReadOnlyList<Border> mrvas) {
    IList<Tuple<Coordinate, Coordinate>> pointLines = convertPointsToLines(routeNavaids);
    mrvas = mrvas.where(q->q.getType() == Border.eType.mrva);

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
    int ret = maxMrvaAlt;
    return ret;
  }

  private static boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
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

  private static Navaid getMainRouteNavaidFromRouteName(String routeName, NavaidList navaids) {
    String name = RegexUtils.extractGroupContent(routeName, "^([A-Z]+)\\d.+", 1);
    Navaid ret = navaids.get(name);
    return ret;
  }

  private static IList<Navaid> evaluateRouteNavaids(IReadOnlyList<IAtcCommand> routeCommands, DARoute.eType type, Navaid mainFix) {
    IList<Navaid> ret = routeCommands
        .where(q -> q instanceof ToNavaidCommand)
        .select(q -> ((ToNavaidCommand) q).getNavaid());

    if (ret.contains(mainFix) == false) {
      switch (type) {
        case sid:
          ret.add(mainFix);
          break;
        case star:
        case transition:
          ret.insert(0, mainFix);
          break;
      }
    }
    return ret;
  }

  public IReadOnlyList<Navaid> getNavaids(){
    IList<Navaid> ret = evaluateRouteNavaids(this.getRouteCommands(), this.type, this.mainNavaid);
    return ret;
  }

  private static double evaluateRouteLength(IReadOnlyList<Navaid> routeNavaids) {
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

  private final eType type;
  private final String name;
  private final PlaneCategoryDefinitions category;
  private final double routeLength;
  private final Navaid mainNavaid;
  private final Integer entryAltitude;
  private final Integer maxMrvaaltitude;

  public DARoute(eType type, String name, PlaneCategoryDefinitions category, Navaid mainNavaid, double routeLength,
                 Integer entryAltitude, IList<IAtcCommand> routeCommands, Integer maxMrvaaltitude,
                 String mapping) {
    super(mapping, routeCommands);
    this.type = type;
    this.name = name;
    this.category = category;
    this.routeLength = routeLength;
    this.mainNavaid = mainNavaid;
    this.entryAltitude = entryAltitude;
    this.maxMrvaaltitude = maxMrvaaltitude;
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
    int ret = maxMrvaaltitude == null ? 0 : maxMrvaaltitude * 100;
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
}


