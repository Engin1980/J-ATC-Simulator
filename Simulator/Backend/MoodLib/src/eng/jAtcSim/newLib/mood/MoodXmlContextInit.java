package eng.jAtcSim.newLib.mood;

import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class MoodXmlContextInit {

  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "mood") == false) return;

    ctx.sdfManager.setSerializer(MoodManager.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(MoodManager.class, new ObjectDeserializer<MoodManager>());

    ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.mood.Mood$Experience", new ObjectSerializer());
  }


}
