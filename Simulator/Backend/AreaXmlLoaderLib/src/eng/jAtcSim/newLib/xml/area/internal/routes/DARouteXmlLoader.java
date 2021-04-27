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
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ToNavaidCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;
import eng.jAtcSim.newLib.xml.speeches.SpeechXmlLoader;

import java.util.Optional;

public class DARouteXmlLoader extends XmlLoader<DARoute> {

  public DARouteXmlLoader(LoadingContext context) {
    super(context);
  }

  @Override
  public DARoute load(XElement source) {
    log(2, "Xml-loading DA-route");
    SmartXmlLoaderUtils.setContext(source);
    String name = SmartXmlLoaderUtils.loadString("name");
    log(3, "... da-route '%s'", name);
    DARouteType type = SmartXmlLoaderUtils.loadEnum("type", DARouteType.class);
    String mapping = SmartXmlLoaderUtils.loadString("mapping");
    PlaneCategoryDefinitions category = SmartXmlLoaderUtils.loadPlaneCategory("category", "ABCD");
    Integer entryAltitude = SmartXmlLoaderUtils.loadAltitude("entryFL", null);
    String mainFixName = SmartXmlLoaderUtils.loadString("mainFix", null);
    Navaid mainNavaid = mainFixName != null ?
            context.area.navaids.get(mainFixName) :
            getMainRouteNavaidFromRouteName(name);

    IList<ICommand> commands = SmartXmlLoaderUtils.loadList(
            source.getChildren(),
            new SpeechXmlLoader(this.context)::load
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
      Optional<Border> mrva = mrvas.tryGetFirst(q -> q.isIn(routePoint.getCoordinate()));
      if (mrva.isPresent())
        maxMrvaAlt = mrva.get().getMaxAltitude();
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
    String name = RegexUtils.extractGroup(routeName, "^([A-Z]+)(\\d.+)?", 1);
    Navaid ret = context.area.navaids.get(name);
    return ret;
  }

  private IReadOnlyList<Navaid> getNavaidsFromCommands(IReadOnlyList<ICommand> commands) {
    IList<String> navaidNames = commands
            .where(q -> q instanceof ToNavaidCommand)
            .select(q -> ((ToNavaidCommand) q).getNavaidName());
    IList<Navaid> ret = navaidNames.select(q -> context.area.navaids.getWithPBD(q));
    return ret;
  }

  private boolean hasMrvaIntersection(IList<Tuple<Coordinate, Coordinate>> pointLines, Border mrva) {
    boolean ret = pointLines.isAny(q -> mrva.hasIntersectionWithLine(q));
    return ret;
  }
}
