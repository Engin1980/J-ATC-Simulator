package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class MessagingXmlContextInit {
  public static void prepareXmlContext(eng.newXmlUtils.XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "messaging") == false) return;

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.messaging");
  }
}
