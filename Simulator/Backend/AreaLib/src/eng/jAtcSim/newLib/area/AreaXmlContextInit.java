package eng.jAtcSim.newLib.area;

import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsDeserializer;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AreaXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx,"area") == false) return;

    ctx.sdfManager.setFormatter(ActiveRunwayThreshold.class, q -> q.getFullName());
    ctx.sdfManager.setParser(ActiveRunwayThreshold.class, (q, c) -> {
      Airport airport = (Airport) c.values.get("airport");
      return airport.getRunwayThreshold(q);
    });

    ctx.sdfManager.setFormatter(Navaid.class, q -> q.getName());
    ctx.sdfManager.setParser(Navaid.class, (q, c) -> {
      Area area = (Area) c.values.get("area");
      return area.getNavaids().get(q);
    });

    ctx.sdfManager.setSerializer(NavaidList.class, new ItemsSerializer());
    ctx.sdfManager.setDeserializer(NavaidList.class, new ItemsDeserializer());

    ctx.sdfManager.setSerializer(RunwayConfiguration.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwayConfiguration.class, new ObjectDeserializer<RunwayConfiguration>());

    ctx.sdfManager.setSerializer(RunwayThresholdConfiguration.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwayThresholdConfiguration.class, new ObjectDeserializer<RunwayConfiguration>());
  }
}
