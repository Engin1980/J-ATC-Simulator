package eng.jAtcSim.newLib.stats;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class StatsXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "stats") == false) return;

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.model");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.properties");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.recent");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.xml");
  }
}
