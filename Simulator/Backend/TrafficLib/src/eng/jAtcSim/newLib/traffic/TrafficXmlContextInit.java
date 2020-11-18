package eng.jAtcSim.newLib.traffic;

import eng.jAtcSim.newLib.shared.CallsignFactory;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class TrafficXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "traffic") == false) return;

    ctx.sdfManager.setSerializer(TrafficProvider.class, new ObjectSerializer()
            .withCustomFieldSerializer("trafficModel", (e, v, c) -> e.setContent("-")));
    ctx.sdfManager.setDeserializer(TrafficProvider.class, new ObjectDeserializer<>()
            .withCustomFieldDeserialization("trafficModel", (e, q) -> q.values.get("trafficModel")));

    ctx.sdfManager.setSerializer(CallsignFactory.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(CallsignFactory.class, new ObjectDeserializer<>());

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.traffic.movementTemplating");

  }
}
