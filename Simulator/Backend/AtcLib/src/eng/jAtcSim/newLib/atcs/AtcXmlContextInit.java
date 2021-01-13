package eng.jAtcSim.newLib.atcs;

import eng.jAtcSim.newLib.area.context.IAreaAcc;
import eng.jAtcSim.newLib.atcs.internal.UserAtc;
import eng.jAtcSim.newLib.atcs.internal.center.CenterAtc;
import eng.jAtcSim.newLib.atcs.internal.tower.RunwayCheckInfo;
import eng.jAtcSim.newLib.atcs.internal.tower.RunwaysInUseInfo;
import eng.jAtcSim.newLib.atcs.internal.tower.SchedulerForAdvice;
import eng.jAtcSim.newLib.atcs.internal.tower.TowerAtc;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ItemsDeserializer;
import eng.newXmlUtils.implementations.ItemsSerializer;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class AtcXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "atc") == false) return;

    ctx.sdfManager.setSerializer(AtcProvider.class, new ObjectSerializer()
            .withIgnoredFields("atcIdsCache", "userAtcIdsCache"));
    ctx.sdfManager.setDeserializer(AtcProvider.class, new ObjectDeserializer<AtcProvider>()
            .withIgnoredFields("atcIdsCache", "userAtcIdsCache")
            .withCustomFieldDeserialization("atcs", new ItemsDeserializer().withInstanceFactory(c ->
                    new AtcList())));
//            .withAfterLoadAction((q, c) -> q.init())); //TODEL

    ctx.sdfManager.setSerializer(AtcList.class, new ItemsSerializer());
    // no des

    // region UserAtc stuff
    ctx.sdfManager.setSerializer(UserAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder"));
    ctx.sdfManager.setDeserializer(UserAtc.class, new ObjectDeserializer<>()
            .withIgnoredFields("recorder"));
    // endregion

    // region CenterAtc stuff
    ctx.sdfManager.setSerializer(CenterAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder", "switchManagerInterface"));
    ctx.sdfManager.setDeserializer(CenterAtc.class, new ObjectDeserializer<>()
            .withIgnoredFields("recorder", "switchManagerInterface"));
    ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.atcs.internal.computer.SwitchManager", new eng.newXmlUtils.implementations.ObjectSerializer()
            .withIgnoredFields("delayedMessagesProducer", "parent"));
    ctx.sdfManager.setDeserializer("eng.jAtcSim.newLib.atcs.internal.computer.SwitchManager", new eng.newXmlUtils.implementations.ObjectDeserializer<>()
            .withIgnoredFields("delayedMessagesProducer", "parent"));

    ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.atcs.internal.computer.SwitchInfo", new eng.newXmlUtils.implementations.ObjectSerializer());
    ctx.sdfManager.setDeserializer("eng.jAtcSim.newLib.atcs.internal.computer.SwitchInfo", new eng.newXmlUtils.implementations.ObjectDeserializer<>());
    // endregion

    // region TowerAtc stuff
    ctx.sdfManager.setSerializer(TowerAtc.class, new ObjectSerializer()
            .withIgnoredFields("recorder", "switchManagerInterface", "onRunwayChanged"));
    ctx.sdfManager.setDeserializer(TowerAtc.class, new ObjectDeserializer<TowerAtc>()
            .withIgnoredFields("recorder", "switchManagerInterface", "onRunwayChanged")
            .withAfterLoadAction((q, c) -> {
              ContextManager.getContext(IAreaAcc.class).setCurrentRunwayConfiguration(q.getRunwayConfigurationInUse());
            }));

    ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.atcs.internal.tower.DepartureManager",
            new eng.newXmlUtils.implementations.ObjectSerializer()
                    .withIgnoredFields("parent", "messageSenderConsumer"));
    ctx.sdfManager.setDeserializer("eng.jAtcSim.newLib.atcs.internal.tower.DepartureManager",
            new eng.newXmlUtils.implementations.ObjectDeserializer<>()
                    .withIgnoredFields("parent", "messageSenderConsumer"));

    ctx.sdfManager.setSerializer("eng.jAtcSim.newLib.atcs.internal.tower.ArrivalManager",
            new eng.newXmlUtils.implementations.ObjectSerializer()
                    .withIgnoredFields("parent", "messageSenderConsumer"));
    ctx.sdfManager.setDeserializer("eng.jAtcSim.newLib.atcs.internal.tower.ArrivalManager",
            new eng.newXmlUtils.implementations.ObjectDeserializer<>()
                    .withIgnoredFields("parent", "messageSenderConsumer"));

    ctx.sdfManager.setSerializer(RunwayCheckInfo.class, new eng.newXmlUtils.implementations.ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwayCheckInfo.class, new eng.newXmlUtils.implementations.ObjectDeserializer<>());

    ctx.sdfManager.setSerializer(SchedulerForAdvice.class, new eng.newXmlUtils.implementations.ObjectSerializer());
    ctx.sdfManager.setDeserializer(SchedulerForAdvice.class, new eng.newXmlUtils.implementations.ObjectDeserializer<>());

    ctx.sdfManager.setSerializer(RunwaysInUseInfo.class, new eng.newXmlUtils.implementations.ObjectSerializer());
    ctx.sdfManager.setDeserializer(RunwaysInUseInfo.class, new eng.newXmlUtils.implementations.ObjectDeserializer<>());
    // endregion
  }
}
