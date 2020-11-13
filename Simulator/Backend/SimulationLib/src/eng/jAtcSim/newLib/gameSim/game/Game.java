package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSimLib.xmlUtils.Parser;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Game implements IGame {

  private Game() {
  }

  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

  public Game(AreaSource areaSource, AirplaneTypesSource airplaneTypesSource, FleetsSource fleetsSource, TrafficSource trafficSource, WeatherSource weatherSource, Simulation simulation) {
    this.areaSource = areaSource;
    this.airplaneTypesSource = airplaneTypesSource;
    this.fleetsSource = fleetsSource;
    this.trafficSource = trafficSource;
    this.weatherSource = weatherSource;
    this.simulation = simulation;
  }

  @Override
  public ISimulation getSimulation() {
    return this.simulation.isim;
  }

  public static void prepareXmlContext(XmlContext ctx) {

    // region saving
    // game
    ctx.sdfManager.setSerializer(Game.class, new ObjectSerializer().withValueClassCheck(Game.class));

    // sources
    ctx.sdfManager.setSerializer(AreaSource.class, new ObjectSerializer().withIgnoredFields("content", "initialized"));
    ctx.sdfManager.setSerializer(AirplaneTypesSource.class, q -> q.getFileName());
    ctx.sdfManager.setSerializer(FleetsSource.class, q -> sf("%s;%s", q.getCompanyFileName(), q.getGeneralAviationFileName()));
    ctx.sdfManager.setSerializer(TrafficXmlSource.class, q -> q.getFileName());

    // sources - weather
    ctx.sdfManager.setSerializer(WeatherXmlSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherXmlSource.class, false)
            .withIgnoredField("content"));
    ctx.sdfManager.setSerializer(WeatherUserSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherUserSource.class, false)
            .withIgnoredField("content"));
    ctx.sdfManager.setSerializer(WeatherOnlineSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherOnlineSource.class, false)
            .withIgnoredField("content"));
    // endregion

    // region loading
    //game
    ctx.sdfManager.setDeserializer(Game.class, new ObjectDeserializer<Game>().withInstanceFactory((c) -> new Game()));

    // sources
    ctx.sdfManager.setDeserializer(AreaSource.class, new ObjectDeserializer<AreaSource>()
            .withIgnoredField("content")
            .withAfterLoadAction((q, c) -> {
              q.init();
              c.values.set("area", q.getArea());
              c.values.set("airport", q.getActiveAirport());
            }));
//    ctx.sdfManager.setDeserializer(AirplaneTypesSource.class, (e, c) -> {
//      AirplaneTypesSource ret = SourceFactory.createAirplaneTypesSource(e.getContent());
//      ret.init();
//      c.values.set("airplaneTypes", ret.getContent());
//      return ret;
//    });
//    ctx.sdfManager.setDeserializer(FleetsSource.class, (e, c) -> {
//      String[] pts = e.getContent().split(";");
//      FleetsSource ret = SourceFactory.createFleetsSource(pts[1], pts[0]);
//      ret.init();
//      return ret;
//    });
//    ctx.sdfManager.setDeserializer(TrafficSource.class, (e, c) -> {
//      TrafficSource ret = SourceFactory.createTrafficXmlSource(e.getContent());
//      ret.init();
//      c.values.set("trafficModel", ret.getContent());
//      return ret;
//    });
//    ctx.sdfManager.setDeserializer(WeatherXmlSource.class, new ObjectDeserializer<WeatherXmlSource>()
//            .withIgnoredField("content")
//            .withAfterLoadAction(q -> q.init()));
//    ctx.sdfManager.setDeserializer(WeatherUserSource.class, new ObjectDeserializer<WeatherUserSource>()
//            .withIgnoredField("content")
//            .withAfterLoadAction(q -> q.init()));
//    ctx.sdfManager.setDeserializer(WeatherOnlineSource.class, new ObjectDeserializer<WeatherOnlineSource>()
//            .withIgnoredField("content")
//            .withAfterLoadAction(q -> q.init()));
    // endregion

//    SimulationStartupContext context = new SimulationStartupContext(
//            game.areaSource.getArea(),
//            game.areaSource.getIcao(),
//            game.airplaneTypesSource.getContent(),
//            game.fleetsSource.getContent().companyFleets,
//            game.fleetsSource.getContent().gaFleets,
//            game.trafficSource.getContent(),
//            game.weatherSource.getContent()
//    );
//    SimulationSettings settings = new SimulationSettings(null, null);
//
//    game.simulation = new Simulation(context, root.getChild("simulation"));













    //game.simulation.load(root.getChild("simulation"));


//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading radar shortcuts...");
//    {
//      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
//      ret.simulation.setCommandShortcuts(shortcuts);
//    }
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading custom data...");
//    {
//      XElement elm = root.getChild("custom");
//      for (XElement child : elm.getChildren()) {
//        String key = child.getName();
//        Object obj = LoadSave.loadFromElement(elm, key, Object.class);
//        customData.set(key, obj);
//      }
//    }
//
//    return ret;

//    return game;
  }

}
