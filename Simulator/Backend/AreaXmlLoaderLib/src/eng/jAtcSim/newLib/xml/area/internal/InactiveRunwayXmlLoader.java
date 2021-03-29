package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.InactiveRunway;
import eng.jAtcSim.newLib.area.InactiveRunwayThreshold;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class InactiveRunwayXmlLoader extends XmlLoader<InactiveRunway> {

  public InactiveRunwayXmlLoader(LoadingContext context) {
    super(context);
  }

  @Override
  public InactiveRunway load(XElement source) {
    log(2, "Xml-loading inactive runway");
    IList<InactiveRunwayThreshold> thresholds = new InactiveRunwayThresholdXmlLoader().loadBoth(
            source.getChild("thresholds").getChildren());
    InactiveRunway ret = new InactiveRunway(thresholds);
    return ret;
  }
}
