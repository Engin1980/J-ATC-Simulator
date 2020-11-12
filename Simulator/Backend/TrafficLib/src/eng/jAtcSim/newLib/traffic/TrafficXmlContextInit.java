package eng.jAtcSim.newLib.traffic;

import eng.jAtcSim.newLib.shared.CallsignFactory;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class TrafficXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx){
    if (XmlContextInit.checkCanBeInitialized(ctx, "traffic")== false) return;

    ctx.sdfManager.setSerializer(CallsignFactory.class, new ObjectSerializer());
    ctx.sdfManager.setSerializer(TrafficProvider.class, new ObjectSerializer()
            .withIgnoredField("trafficModel"));
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.traffic.movementTemplating");
  }
}
