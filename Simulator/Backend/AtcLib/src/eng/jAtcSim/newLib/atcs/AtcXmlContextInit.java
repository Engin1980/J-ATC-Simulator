package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.atcs.internal.UserAtc;
import eng.jAtcSim.newLib.atcs.internal.center.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.computer.ComputerAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AtcXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "atc") == false) return;

    ctx.sdfManager.setSerializer(AtcProvider.class, new ObjectSerializer()
            .withIgnoredFields("atcIdsCache", "userAtcIdsCache"));
    ctx.sdfManager.setSerializer(AtcList.class, new ItemsSerializer());
    ctx.sdfManager.setSerializer(UserAtc.class, new ObjectSerializer()
            .withIgnoredField("recorder"));
    ComputerAtc.prepareXmlContext(ctx);
    ctx.sdfManager.setSerializer(CenterAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder", "switchManagerInterface"));
    ctx.sdfManager.setSerializer(TowerAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder", "switchManagerInterface", "onRunwayChanged"));
    TowerAtc.prepareXmlContext(ctx);
  }
}
