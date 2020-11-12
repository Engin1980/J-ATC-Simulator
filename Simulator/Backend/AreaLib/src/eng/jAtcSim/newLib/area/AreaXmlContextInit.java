package eng.jAtcSim.newLib.area;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AreaXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx,"area") == false) return;

    ctx.sdfManager.setFormatter(ActiveRunwayThreshold.class, q -> q.getFullName());
    ctx.sdfManager.setFormatter(Navaid.class, q -> q.getName());
    ctx.sdfManager.setSerializer(NavaidList.class, new ItemsSerializer());
    ctx.sdfManager.setSerializer(RunwayConfiguration.class, new ObjectSerializer());
    ctx.sdfManager.setSerializer(RunwayThresholdConfiguration.class, new ObjectSerializer());
  }
}
