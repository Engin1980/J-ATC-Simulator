package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.oldApproaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class ActiveRunwayThresholdXmlLoader {
  private final XmlMappingDictinary<DARoute> mappings;

  public ActiveRunwayThresholdXmlLoader(XmlMappingDictinary<DARoute> mappings) {
    this.mappings = mappings;
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
    String mapping = XmlLoaderUtils.loadString("mapping");

    IList<DARoute> routes = mappings.get(mapping);

    IList<Approach> approaches = XmlLoaderUtils.loadList(
        source.getChild("approaches").getChildren(),
        new ApproachXmlLoader());

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
        name, coordinate, initialDepartureAltitude,
        approaches, routes);
    return ret;
  }
}
