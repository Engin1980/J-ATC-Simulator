package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.approaches.ApproachXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class ActiveRunwayThresholdXmlLoader extends XmlLoader<ActiveRunwayThreshold> {

  protected ActiveRunwayThresholdXmlLoader(Context context) {
    super(context);
  }

  //TODO solve this
  @Override
  public ActiveRunwayThreshold load(XElement source) {
    throw new UnsupportedOperationException("This method is here not supported. Use 'loadBoth()' instead.");
  }

  IList<ActiveRunwayThreshold> loadBoth(IReadOnlyList<XElement> sources) {
    assert sources.size() == 2 : "There must be two thresholds";

    ActiveRunwayThreshold.Prototype ia = loadOne(sources.get(0));
    ActiveRunwayThreshold.Prototype ib = loadOne(sources.get(1));

    IList<ActiveRunwayThreshold> ret = ActiveRunwayThreshold.create(ia, ib);
    return ret;
  }

  public ActiveRunwayThreshold.Prototype loadOne(XElement source) {
    XmlLoaderUtils.setContext(source);
    String name = XmlLoaderUtils.loadString("name");
    Coordinate coordinate = XmlLoaderUtils.loadCoordinate("coordinate");
    int initialDepartureAltitude = XmlLoaderUtils.loadInteger("initialDepartureAltitude");
    String mapping = XmlLoaderUtils.loadString("mapping");

    IList<DARoute> routes = context.airport.daMappings.get(mapping);

    IList<Approach> approaches = XmlLoaderUtils.loadList(
        source.getChild("approaches").getChildren(),
        new ApproachXmlLoader(context));

    ActiveRunwayThreshold.Prototype ret = new ActiveRunwayThreshold.Prototype(
        name, coordinate, initialDepartureAltitude,
        approaches, routes);
    return ret;
  }
}
