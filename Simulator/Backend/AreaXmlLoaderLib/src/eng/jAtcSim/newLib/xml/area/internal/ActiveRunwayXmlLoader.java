package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.ActiveRunway;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class ActiveRunwayXmlLoader extends XmlLoader<ActiveRunway> {


  protected ActiveRunwayXmlLoader(Context context) {
    super(context);
  }

  @Override
  public ActiveRunway load(XElement source) {
    log(2, "Xml-loading active runway");
    IList<ActiveRunwayThreshold> thresholds = new ActiveRunwayThresholdXmlLoader(context).loadBoth(
        source.getChild("thresholds").getChildren());
    ActiveRunway ret = new ActiveRunway(thresholds);
    return ret;
  }


}
