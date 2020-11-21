package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.utilites.ReflectionUtils;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypes;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.area.Border;
import eng.jAtcSim.newLib.atcs.AtcXmlContextInit;
import eng.jAtcSim.newLib.fleet.airliners.AirlinesFleets;
import eng.jAtcSim.newLib.fleet.generalAviation.GeneralAviationFleets;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.AirproxController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.EmergencyAppearanceController;
import eng.jAtcSim.newLib.gameSim.simulation.controllers.MrvaController;
import eng.jAtcSim.newLib.gameSim.simulation.modules.*;
import eng.jAtcSim.newLib.stats.StatsProvider;
import eng.jAtcSim.newLib.stats.StatsXmlContextInit;
import eng.jAtcSim.newLib.weather.WeatherXmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class SimulationXmlContextInit {
  public static void prepareXmlContext(XmlContext ctx) {
    // region Simulation
    ctx.sdfManager.setSerializer(Simulation.class, new ObjectSerializer()
            .withValueClassCheck(Simulation.class, false)
            .withIgnoredFields(
                    "ioModule", // nothing to save
                    "isim", // accessor, not to save
                    "worldModule" // not saved
            ));
    ctx.sdfManager.setDeserializer(Simulation.class, new ObjectDeserializer<Simulation>()
            .withIgnoredFields("ioModule", "isim", "worldModule")
            .withBeforeLoadAction((q, c) -> c.values.set(q))
            .withAfterLoadAction((q, c) -> {
              WorldModule worldModule = new WorldModule(
                      q,
                      c.values.get(Area.class),
                      c.values.get(Airport.class),
                      c.values.get(AirplaneTypes.class),
                      c.values.get(AirlinesFleets.class),
                      c.values.get(GeneralAviationFleets.class));
              ReflectionUtils.FieldUtils.setFieldValue(q, "worldModule", worldModule);
              c.values.remove(q);
              q.reinitAfterLoad();
            }));

    // endregion

    // region AirplanesModule
    ctx.sdfManager.setSerializer(AirplanesModule.class, new ObjectSerializer()
            .withValueClassCheck(AirplanesModule.class)
            .withIgnoredFields("planes4public")
            .withCustomFieldFormatter("parent", q -> "-"));
    ctx.sdfManager.setDeserializer(AirplanesModule.class, new ObjectDeserializer<AirplanesModule>()
            .withIgnoredFields("planes4public")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get(Simulation.class))
            .withAfterLoadAction((q, c) -> q.init()));

    ctx.sdfManager.setSerializer(AirproxController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(AirproxController.class, new ObjectDeserializer<AirproxController>());

    ctx.sdfManager.setSerializer(EmergencyAppearanceController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(EmergencyAppearanceController.class, new ObjectDeserializer<EmergencyAppearanceController>());

    ctx.sdfManager.setSerializer(MrvaController.class,
            new ObjectSerializer()
                    .withIgnoredFields("mrvas", "mrvaMaps"));
    ctx.sdfManager.setDeserializer(MrvaController.class, new ObjectDeserializer<MrvaController>()
            .withInstanceFactory(c -> new MrvaController(c.values.get(Area.class).getBorders().where(q -> q.getType() == Border.eType.mrva)))
            .withIgnoredFields("mrvas", "mrvaMaps"));

    ctx.sdfManager.addAutoPackage("eng.jAtcSim.newLib.airplanes.templates");
    // endregion

    // region AtcModule
    ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer()
            .withIgnoredFields("userAtcsCache")
            .withCustomFieldFormatter("parent", q -> "-"));
    ctx.sdfManager.setDeserializer(AtcModule.class, new ObjectDeserializer<AtcModule>()
            .withIgnoredFields("userAtcsCache")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get(Simulation.class)));

    AtcXmlContextInit.prepareXmlContext(ctx);

    // endregion

    // region StatsModule
    ctx.sdfManager.setSerializer(StatsModule.class, new ObjectSerializer()
            .withCustomFieldFormatter("parent", q -> "-"));
    ctx.sdfManager.setDeserializer(StatsModule.class, new ObjectDeserializer<StatsModule>()
            .withInstanceFactory(c -> new StatsModule(null, new StatsProvider(3)))
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get(Simulation.class)));

    StatsXmlContextInit.prepareXmlContext(ctx);
    // endregion

    // region TimerModule
    ctx.sdfManager.setFormatter(TimerModule.class, q -> Integer.toString(q.getTickInterval()));
    ctx.sdfManager.setDeserializer(TimerModule.class, (e, c) -> {
      Simulation sim = c.values.get(Simulation.class);
      int tickInterval = Integer.parseInt(e.getContent().substring(1));
      TimerModule ret = new TimerModule(sim, tickInterval);
      return ret;
    });
    // endregion

    // region TrafficModule
    ctx.sdfManager.setSerializer(TrafficModule.class, new ObjectSerializer()
            .withCustomFieldFormatter("parent", q -> "-"));
    ctx.sdfManager.setDeserializer(TrafficModule.class, new ObjectDeserializer<TrafficModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get(Simulation.class)));
    // endregion

    // region WeatherModule
    ctx.sdfManager.setSerializer(WeatherModule.class, new ObjectSerializer()
            .withCustomFieldFormatter("parent", q -> "-"));
    ctx.sdfManager.setDeserializer(WeatherModule.class, new ObjectDeserializer<WeatherModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get(Simulation.class))
            .withAfterLoadAction((q, c) -> q.init()));

    WeatherXmlContextInit.prepareXmlContext(ctx);
    // endregion
  }
}
