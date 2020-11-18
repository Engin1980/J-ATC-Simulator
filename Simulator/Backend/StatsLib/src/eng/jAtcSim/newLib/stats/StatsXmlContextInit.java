package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.EList;
import eng.jAtcSim.newLib.shared.xml.XmlContextInit;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureModel;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.newLib.stats.properties.CounterProperty;
import eng.jAtcSim.newLib.stats.properties.StatisticProperty;
import eng.jAtcSim.newLib.stats.recent.RecentStats;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class StatsXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    if (XmlContextInit.checkCanBeInitialized(ctx, "stats") == false) return;

    ctx.sdfManager.setDeserializer(Collector.class, new ObjectDeserializer<Collector>()
            .withCustomFieldDeserialization("planesInSim",
                    new ObjectDeserializer<>().withInstanceFactory(c -> new ArrivalDepartureTotalModel<>(
                            new StatisticProperty(), new StatisticProperty(), new StatisticProperty())))
            .withCustomFieldDeserialization("planesUnderApp",
                    new ObjectDeserializer<>().withInstanceFactory(c -> new ArrivalDepartureTotalModel<>(new StatisticProperty(), new StatisticProperty(), new StatisticProperty())))
            .withCustomFieldDeserialization("runwayMovements",
                    new ObjectDeserializer<>().withInstanceFactory(c -> new ArrivalDepartureModel<>(new CounterProperty(), new CounterProperty())))
            .withCustomFieldDeserialization("finishedPlanesDelays",
                    new ObjectDeserializer<>().withInstanceFactory(c -> new ArrivalDepartureModel<>(new StatisticProperty(), new StatisticProperty())))
            .withCustomFieldDeserialization("finishedPlanesMoods",
                    new ObjectDeserializer<>().withInstanceFactory(c -> new ArrivalDepartureModel<>(new EList<>(), new EList<>())))
    );

    ctx.sdfManager.setSerializer(RecentStats.class, new ObjectSerializer()
            .withIgnoredFields("clsErrors", "clsDelays", "clsHP", "clsMovements", "clsCurrent", "clsFinished"));
    ctx.sdfManager.setDeserializer(RecentStats.class, new ObjectDeserializer<>()
            .withIgnoredFields("clsErrors", "clsDelays", "clsHP", "clsMovements", "clsCurrent", "clsFinished"));

    ctx.sdfManager.setSerializer(StatsProvider.class, new ObjectSerializer()
            .withIgnoredFields("myStatsProvider"));
    ctx.sdfManager.setDeserializer(StatsProvider.class, new ObjectDeserializer<>()
            .withIgnoredFields("myStatsProvider"));

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.model");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.properties");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.recent");
    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.stats.xml");
  }
}
