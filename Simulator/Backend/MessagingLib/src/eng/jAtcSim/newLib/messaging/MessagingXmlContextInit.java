package eng.jAtcSim.newLib.messaging;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;

public class MessagingXmlContextInit {
  public static void prepareXmlContext(eng.newXmlUtils.XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "messaging") == false) return;
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.messaging");
  }
}
