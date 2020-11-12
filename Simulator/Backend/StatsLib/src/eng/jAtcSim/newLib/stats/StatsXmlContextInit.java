package eng.jAtcSim.newLib.stats;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;

public class StatsXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "stats") == false) return;

    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.model");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.properties");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.recent");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.xml");
  }
}
