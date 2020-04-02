package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;

public class ActiveRunwayXmlLoader implements IXmlLoader<ActiveRunway> {

  private final IReadOnlyList<DARoute> daRoutes;

  public ActiveRunwayXmlLoader(IReadOnlyList<DARoute> daRoutes) {
    this.daRoutes = daRoutes;
  }

  @Override
  public ActiveRunway load(XElement source) {
    IList<ActiveRunwayThreshold> thresholds = new ActiveRunwayThresholdXmlLoader(daRoutes).loadBoth(
        source.getChild("thresholds").getChildren());
    ActiveRunway ret = new ActiveRunway(thresholds);
    return ret;
  }


}
