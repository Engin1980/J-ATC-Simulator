package eng.jAtcSim.newLib.gameSim.simulation;

import eng.eSystem.eXml.XElement;
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
import eng.jAtcSim.newLib.stats.StatsXmlContextInit;
import eng.jAtcSim.newLib.weather.WeatherXmlContextInit;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class SimulationXmlContextIniti {
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
            .withAfterLoadAction((q, c) -> {
              WorldModule worldModule = new WorldModule(
                      q,
                      (Area) c.values.get("area"),
                      (Airport) c.values.get("airport"),
                      (AirplaneTypes) c.values.get("airplaneTypes"),
                      (AirlinesFleets) c.values.get("companyFleets"),
                      (GeneralAviationFleets) c.values.get("gaFleets"));
              ReflectionUtils.FieldUtils.set(q, "worldModule", worldModule);
              c.values.set("simulation", q);
            }));

    // endregion

    // region AirplanesModule
    ctx.sdfManager.setSerializer(AirplanesModule.class, new ObjectSerializer()
            .withValueClassCheck(AirplanesModule.class)
            .withIgnoredFields("planes4public")
            .withIgnoredFields("parent"));

    ctx.sdfManager.setDeserializer(AirplanesModule.class, new ObjectDeserializer<AirplanesModule>()
            .withIgnoredFields("planes4public")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation"))
            .withAfterLoadAction((q, c) -> q.init()));

    ctx.sdfManager.setSerializer(AirproxController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(AirproxController.class, new ObjectDeserializer<AirproxController>());

    ctx.sdfManager.setSerializer(EmergencyAppearanceController.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(EmergencyAppearanceController.class, new ObjectDeserializer<EmergencyAppearanceController>());

    ctx.sdfManager.setSerializer(MrvaController.class,
            new ObjectSerializer()
                    .withIgnoredFields("mrvas", "mrvaMaps"));
    ctx.sdfManager.setDeserializer(MrvaController.class, new ObjectDeserializer<MrvaController>()
            .withInstanceFactory(c -> new MrvaController(((Area) c.values.get("area")).getBorders().where(q -> q.getType() == Border.eType.mrva)))
            .withIgnoredFields("mrvas", "mrvaMaps"));

    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.airplanes.templates");
    // endregion

    // region AtcModule
    ctx.sdfManager.setSerializer(AtcModule.class, new ObjectSerializer()
            .withIgnoredFields("userAtcsCache")
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(AtcModule.class, new ObjectDeserializer<AtcModule>()
            .withIgnoredFields("userAtcsCache")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    AtcXmlContextInit.prepareXmlContext(ctx);

    // endregion

    // region StatsModule
    ctx.sdfManager.setSerializer(StatsModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(StatsModule.class, new ObjectDeserializer<StatsModule>()
            .withIgnoredFields("parent")
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    StatsXmlContextInit.prepareXmlContext(ctx);
    // endregion

    // region TimerModule
    ctx.sdfManager.setSerializer(TimerModule.class, (XElement e, Object v, XmlContext c) -> {
      TimerModule t = (TimerModule) v;
      if (t.isRunning())
        e.setContent("y" + t.getTickInterval());
      else
        e.setContent("n" + t.getTickInterval());
    });
    ctx.sdfManager.setDeserializer(TimerModule.class, (e, c) -> {
      Simulation sim = (Simulation) c.values.get("simulation");
      int tickInterval = Integer.parseInt(e.getContent().substring(1));
      TimerModule ret = new TimerModule(sim, tickInterval);
      if (e.getContent().charAt(0) == 'y')
        ret.start();
      return ret;
    });
    // endregion

    // region TrafficModule
    ctx.sdfManager.setSerializer(TrafficModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(TrafficModule.class, new ObjectDeserializer<TrafficModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));
    // endregion

    // region WeatherModule
    ctx.sdfManager.setSerializer(WeatherModule.class, new ObjectSerializer()
            .withIgnoredFields("parent"));
    ctx.sdfManager.setDeserializer(WeatherModule.class, new ObjectDeserializer<WeatherModule>()
            .withCustomFieldDeserialization("parent", (e, c) -> c.values.get("simulation")));

    WeatherXmlContextInit.prepareXmlContext(ctx);
    // endregion
  }
}
