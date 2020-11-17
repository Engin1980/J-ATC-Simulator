package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EDistinctList;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.atcs.internal.Atc;
import eng.jAtcSim.newLib.atcs.internal.UserAtc;
import eng.jAtcSim.newLib.atcs.internal.center.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.computer.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AtcXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "atc") == false) return;

    ctx.sdfManager.setSerializer(AtcProvider.class, new ObjectSerializer()
            .withIgnoredFields("atcIdsCache", "userAtcIdsCache"));
    ctx.sdfManager.setDeserializer(AtcProvider.class, new ObjectDeserializer<>()
            .withInstanceFactory(c -> new AtcProvider(c.values.get(Airport.class)))
            .withCustomFieldDeserialization("atcs", (e, c) -> new AtcList<Atc>(
                    q -> q.getAtcId(), EDistinctList.Behavior.exception)));

    ctx.sdfManager.setSerializer(AtcList.class, new ItemsSerializer());
    // no des

    ctx.sdfManager.setSerializer(UserAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder"));
    ctx.sdfManager.setDeserializer(UserAtc.class, new ObjectDeserializer<>()
            .withIgnoredFields("recorder"));

    ComputerAtc.prepareXmlContext(ctx);

    ctx.sdfManager.setSerializer(CenterAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder", "switchManagerInterface"));
    ctx.sdfManager.setDeserializer(CenterAtc.class, new ObjectDeserializer<>()
            .withIgnoredFields("recorder", "switchManagerInterface"));

    TowerAtc.prepareXmlContext(ctx);
  }
}
