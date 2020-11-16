package eng.jAtcSim.newLib.stats;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class StatsXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "stats") == false) return;

    ctx.sdfManager.addAutoPackageSerializer("eng.jAtcSim.newLib.stats", ObjectSerializer::new);
    ctx.sdfManager.addAutoPackageDeserializer("eng.jAtcSim.newLib.stats", ObjectDeserializer::new);

    ctx.sdfManager.addAutoPackageSerializer("eng.jAtcSim.newLib.stats.model", ObjectSerializer::new);
    ctx.sdfManager.addAutoPackageDeserializer("eng.jAtcSim.newLib.stats.model", ObjectDeserializer::new);

    ctx.sdfManager.addAutoPackageSerializer("eng.jAtcSim.newLib.stats.properties", ObjectSerializer::new);
    ctx.sdfManager.addAutoPackageDeserializer("eng.jAtcSim.newLib.stats.properties", ObjectDeserializer::new);

    ctx.sdfManager.addAutoPackageSerializer("eng.jAtcSim.newLib.stats.recent", ObjectSerializer::new);
    ctx.sdfManager.addAutoPackageDeserializer("eng.jAtcSim.newLib.stats.recent", ObjectDeserializer::new);

    ctx.sdfManager.addAutoPackageSerializer("eng.jAtcSim.newLib.stats.xml", ObjectSerializer::new);
    ctx.sdfManager.addAutoPackageDeserializer("eng.jAtcSim.newLib.stats.xml", ObjectDeserializer::new);
  }
}
