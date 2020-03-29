package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.geo.Coordinates;
import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class ActiveRunwayThresholdXmlLoader {
  private final IReadOnlyList<DARoute> daRoutes;

  public ActiveRunwayThresholdXmlLoader(IReadOnlyList<DARoute> daRoutes) {
    this.daRoutes = daRoutes;
  }

  IList<ActiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold.Prototype ia = load(sources.get(0));
    ActiveRunwayThreshold.Prototype ib = load(sources.get(1));

    IList<ActiveRunwayThreshold> ret = ActiveRunwayThreshold.create(ia, ib);
    return ret;
  }

  private ActiveRunwayThreshold.Prototype load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    Coordinate coordinate = XmlLoaderUtils.loadCoordinate("coordinate");
    int initialDepartureAltitude = XmlLoaderUtils.loadInteger("initialDepartureAltitude");
    String mappingString = XmlLoaderUtils.loadString("mapping");
    IList<String> mapping = new EList<>(mappingString.split(";"));

    IList<DARoute> routes = daRoutes.where(q -> q.isMappingMatch(mapping));

    IList<Approach> approaches = XmlLoaderUtils.loadList(
        source.getChild("approaches").getChildren(),
        new ApproachXmlLoader());

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
        name, coordinate, initialDepartureAltitude,
        approaches, routes);
    return ret;
  }
}
