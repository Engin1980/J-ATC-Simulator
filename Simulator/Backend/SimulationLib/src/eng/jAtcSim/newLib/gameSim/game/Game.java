package eng.jAtcSim.newLib.gameSim.game;

import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.ContextManager;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Game implements IGame {

  public static void prepareXmlContext(XmlContext ctx) {

    // region Game
    ctx.sdfManager.setSerializer(Game.class, new ObjectSerializer().withValueClassCheck(Game.class));
    ctx.sdfManager.setDeserializer(Game.class, new ObjectDeserializer<Game>().withInstanceFactory((c) -> new Game()));
    // endregion

    // region Sources
    ctx.sdfManager.setSerializer(AreaSource.class, new ObjectSerializer()
            .withIgnoredFields("content", "initialized"));
    ctx.sdfManager.setDeserializer(AreaSource.class, new ObjectDeserializer<AreaSource>()
            .withIgnoredFields("content", "initialized")
            .withAfterLoadAction((q, c) -> {
              q.init();
              c.values.set(q.getArea());
              c.values.set(q.getActiveAirport());
              c.sdfManager.setParser(AtcId.class, (qq, cc) -> q.getActiveAirport().getAtcTemplates().select(qqq -> qqq.toAtcId()).getFirst(qqq -> qqq.getName().equals(qq)));

              SharedAcc sharedContext = new SharedAcc(
                      q.getActiveAirport().getIcao(),
                      q.getActiveAirport().getAtcTemplates().select(qq -> qq.toAtcId()),
                      new EDayTimeRun(0),
                      new SimulationLog()
              );
              ContextManager.setContext(ISharedAcc.class, sharedContext);
            }));

    ctx.sdfManager.setFormatter(AirplaneTypesSource.class, q -> q.getFileName());
    ctx.sdfManager.setDeserializer(AirplaneTypesSource.class, (e, c) -> {
      AirplaneTypesSource ret = SourceFactory.createAirplaneTypesSource(e.getContent());
      ret.init();
      c.values.set(ret.getContent());
      return ret;
    });

    ctx.sdfManager.setFormatter(FleetsSource.class, q -> sf("%s;%s", q.getCompanyFileName(), q.getGeneralAviationFileName()));
    ctx.sdfManager.setDeserializer(FleetsSource.class, (e, c) -> {
      String[] pts = e.getContent().split(";");
      FleetsSource ret = SourceFactory.createFleetsSource(pts[1], pts[0]);
      ret.init();
      c.values.set(ret.getContent().companyFleets);
      c.values.set(ret.getContent().gaFleets);
      return ret;
    });

    ctx.sdfManager.setFormatter(TrafficXmlSource.class, q -> q.getFileName());
    ctx.sdfManager.setDeserializer(TrafficSource.class, (e, c) -> {
      TrafficSource ret = SourceFactory.createTrafficXmlSource(e.getContent());
      ret.init();
      c.values.set("trafficModel", ret.getContent());
      return ret;
    });

    // sources - weather
    ctx.sdfManager.setSerializer(WeatherXmlSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherXmlSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherXmlSource.class, new ObjectDeserializer<WeatherXmlSource>()
            .withIgnoredFields("content")
            .withAfterLoadAction((q, c) -> {
              q.init();
              c.values.set("weatherProvider", q.getContent());
            }));

    ctx.sdfManager.setSerializer(WeatherUserSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherUserSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherUserSource.class, new ObjectDeserializer<WeatherUserSource>()
            .withIgnoredFields("content")
            .withAfterLoadAction((q, c) -> {
              q.init();
              c.values.set("weatherProvider", q.getContent());
            }));

    ctx.sdfManager.setSerializer(WeatherOnlineSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherOnlineSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherOnlineSource.class, new ObjectDeserializer<WeatherOnlineSource>()
            .withIgnoredFields("content")
            .withAfterLoadAction((q, c) -> {
              q.init();
              c.values.set("weatherProvider", q.getContent());
            }));
    // endregion sources


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

  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

  private Game() {
  }

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

}
