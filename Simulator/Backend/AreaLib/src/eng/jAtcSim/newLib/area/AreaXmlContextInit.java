package eng.jAtcSim.newLib.area;

import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsDeserializer;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AreaXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "area") == false) return;

    ctx.sdfManager.setFormatter(ActiveRunwayThreshold.class, q -> q.getName());
    ctx.sdfManager.setParser(ActiveRunwayThreshold.class, (q, c) -> c.values.get(Airport.class).getRunwayThreshold(q));

    ctx.sdfManager.setFormatter(Navaid.class, q -> q.getName());
    ctx.sdfManager.setParser(Navaid.class, (q, c) -> c.values.get(Area.class).getNavaids().get(q));

    ctx.sdfManager.setSerializer(NavaidList.class, new ItemsSerializer());
    ctx.sdfManager.setDeserializer(NavaidList.class, new ItemsDeserializer());

    ctx.sdfManager.setSerializer(RunwayConfiguration.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwayConfiguration.class, new ObjectDeserializer<RunwayConfiguration>());

    ctx.sdfManager.setSerializer(RunwayThresholdConfiguration.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwayThresholdConfiguration.class, new ObjectDeserializer<RunwayConfiguration>());

    ctx.sdfManager.setSerializer(EntryExitPoint.class, new ObjectSerializer()
            .withCustomFieldSerializer("parent", (e, v, c) -> e.setContent("-")));
    ctx.sdfManager.setDeserializer(EntryExitPoint.class, new ObjectDeserializer<>()
            .withCustomFieldDeserialization("parent", (e, c) -> ContextManager.getContext(IAreaAcc.class).getAirport()));

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.area.approaches");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.area.approaches.behaviors");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.area.approaches.conditions");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.area.approaches.conditions.locations");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.area.approaches.perCategoryValues");
  }
}
