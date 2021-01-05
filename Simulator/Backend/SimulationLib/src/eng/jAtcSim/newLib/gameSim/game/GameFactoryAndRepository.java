package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.functionalInterfaces.Consumer2;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplaneType.AirplaneTypesXmlContextInit;
import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.AirplaneXmlContextInit;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.AreaXmlContextInit;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationXmlContextInit;
import eng.jAtcSim.newLib.messaging.MessagingXmlContextInit;
import eng.jAtcSim.newLib.mood.MoodXmlContextInit;
import eng.jAtcSim.newLib.shared.*;
import eng.jAtcSim.newLib.shared.context.ISharedAcc;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.logging.SimulationLog;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSim.newLib.traffic.TrafficXmlContextInit;
import eng.jAtcSim.newLib.weather.Weather;
import eng.jAtcSim.newLib.weather.WeatherProvider;
import eng.newXmlUtils.SDFFactory;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectDeserializer;
import eng.newXmlUtils.implementations.ObjectSerializer;
import exml.XContext;

import java.util.ArrayList;
import java.util.Map;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class GameFactoryAndRepository {
  private static void prepareXmlContextForSources(XmlContext ctx) {
    ctx.sdfManager.setSerializers(SDFFactory.getSimpleSerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getSimpleDeserializers());

    ctx.sdfManager.setSerializers(SDFFactory.getSimpleArraySerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getSimpleArrayDeserializers());

    ctx.sdfManager.setSerializers(SDFFactory.getESystemSerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getESystemDeserializers());

    ctx.sdfManager.setSerializers(SharedXmlUtils.Serializers.serializers);
    ctx.sdfManager.setDeserializers(SharedXmlUtils.Deserializers.deserializers);

    // region Sources
    ctx.sdfManager.setSerializer(AreaSource.class, new ObjectSerializer()
            .withIgnoredFields("content", "initialized"));
    ctx.sdfManager.setDeserializer(AreaSource.class, new ObjectDeserializer<AreaSource>()
            .withIgnoredFields("content", "initialized"));

    ctx.sdfManager.setFormatter(AirplaneTypesSource.class, q -> q.getFileName());
    ctx.sdfManager.setDeserializer(AirplaneTypesSource.class, (e, c) -> SourceFactory.createAirplaneTypesSource(e.getContent()));

    ctx.sdfManager.setFormatter(FleetsSource.class, q -> sf("%s;%s", q.getCompanyFileName(), q.getGeneralAviationFileName()));
    ctx.sdfManager.setDeserializer(FleetsSource.class, (e, c) -> {
      String[] pts = e.getContent().split(";");
      FleetsSource ret = SourceFactory.createFleetsSource(pts[1], pts[0]);
      return ret;
    });

    ctx.sdfManager.setFormatter(TrafficXmlSource.class, q -> q.getFileName());
    ctx.sdfManager.setDeserializer(TrafficSource.class, (e, c) -> SourceFactory.createTrafficXmlSource(e.getContent()));

    // sources - weather
    ctx.sdfManager.setSerializer(WeatherXmlSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherXmlSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherXmlSource.class, new ObjectDeserializer<WeatherXmlSource>()
            .withIgnoredFields("content"));

    ctx.sdfManager.setSerializer(WeatherUserSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherUserSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherUserSource.class, new ObjectDeserializer<WeatherUserSource>()
            .withIgnoredFields("content"));

    ctx.sdfManager.setSerializer(WeatherOnlineSource.class, new ObjectSerializer()
            .withValueClassCheck(WeatherOnlineSource.class, false)
            .withIgnoredFields("content"));
    ctx.sdfManager.setDeserializer(WeatherOnlineSource.class, new ObjectDeserializer<WeatherOnlineSource>()
            .withIgnoredFields("content"));

    ctx.sdfManager.setSerializer(Weather.class, new ObjectSerializer());
    ctx.sdfManager.setDeserializer(Weather.class, new ObjectDeserializer<>());
    // endregion sources
  }

  private static void prepareXmlContextForSimulation(XmlContext ctx) {
    ctx.sdfManager.setSerializers(SDFFactory.getSimpleSerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getSimpleDeserializers());

    ctx.sdfManager.setSerializers(SDFFactory.getSimpleArraySerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getSimpleArrayDeserializers());

    ctx.sdfManager.setSerializers(SDFFactory.getESystemSerializers());
    ctx.sdfManager.setDeserializers(SDFFactory.getESystemDeserializers());

    ctx.sdfManager.setSerializers(SharedXmlUtils.Serializers.serializers);
    ctx.sdfManager.setDeserializers(SharedXmlUtils.Deserializers.deserializers);

    AreaXmlContextInit.prepareXmlContext(ctx);
    AirplaneTypesXmlContextInit.prepareXmlContext(ctx);
    MessagingXmlContextInit.prepareXmlContext(ctx);
    MoodXmlContextInit.prepareXmlContext(ctx);
    AirplaneXmlContextInit.prepareXmlContext(ctx);
    TrafficXmlContextInit.prepareXmlContext(ctx);

    SimulationXmlContextInit.prepareXmlContext(ctx);
  }

  public Game create(GameStartupInfo gsi) {
    Game game;
    ApplicationLog appLog = Context.getApp().getAppLog();

    Simulation simulation;

    try {
      appLog.write(ApplicationLog.eType.info, "Loading area");
      EAssert.isNotNull(gsi.areaSource, "Area-Source not set.");
      gsi.areaSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize area.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading plane types");
      EAssert.isNotNull(gsi.airplaneTypesSource, "Airplane-Type-Source not set.");
      gsi.airplaneTypesSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize plane types.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading fleets");
      EAssert.isNotNull(gsi.fleetsSource, "Fleet-Source not set.");
      gsi.fleetsSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize fleets.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Loading traffic");
      EAssert.isNotNull(gsi.trafficSource, "Traffic-Source not set.");
      gsi.trafficSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Initializing weather");
      EAssert.isNotNull(gsi.weatherSource, "Weather-Source not set.");
      gsi.weatherSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load, download or initialize weather.", ex);
    }

    PostContracts.checkAndClear();

    try {
      appLog.write(ApplicationLog.eType.info, "Creating the simulation");
      SimulationStartupContext simulationContext = new SimulationStartupContext(
              gsi.areaSource.getContent(),
              gsi.areaSource.getIcao(),
              gsi.airplaneTypesSource.getContent(),
              gsi.fleetsSource.getContent().companyFleets,
              gsi.fleetsSource.getContent().gaFleets,
              gsi.trafficSource.getContent(),
              gsi.weatherSource.getContent()
      );

      SimulationSettings simulationSettings = new SimulationSettings(
              gsi.trafficSettings,
              gsi.simulationSettings
      );

      simulation = new Simulation(simulationContext, simulationSettings);
      simulation.init();
      game = new Game(
              gsi.areaSource,
              gsi.airplaneTypesSource,
              gsi.fleetsSource,
              gsi.trafficSource,
              gsi.weatherSource,
              simulation
      );
      appLog.write(ApplicationLog.eType.info, "Initializing the simulation");
    } catch (Exception ex) {
      throw new EApplicationException("Unable to create or initialize the simulation.", ex);
    }

    PostContracts.checkAndClear();

    return game;
  }

  public Game load(String fileName) {
    //TODO update with custom data
    IMap<String, Object> customData = new EMap<>();

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();

    XmlContext ctx = new XmlContext();
    prepareXmlContextForSources(ctx);
    prepareXmlContextForSimulation(ctx);

    PostContracts.checkAndClear();

    GameStartupInfo gsi = new GameStartupInfo();
    gsi.areaSource = XmlContext.deserialize(root.getChild("areaSource"), ctx, AreaSource.class);
    gsi.airplaneTypesSource = XmlContext.deserialize(root.getChild("airplaneTypesSource"), ctx, AirplaneTypesSource.class);
    gsi.fleetsSource = XmlContext.deserialize(root.getChild("fleetsSource"), ctx, FleetsSource.class);
    gsi.trafficSource = XmlContext.deserialize(root.getChild("trafficSource"), ctx, TrafficSource.class);
    gsi.weatherSource = (WeatherSource) XmlContext.deserialize(root.getChild("weatherSource"), ctx);

    gsi.areaSource.init();
    gsi.airplaneTypesSource.init();
    gsi.fleetsSource.init();
    gsi.trafficSource.init();
    gsi.weatherSource.init();

    PostContracts.checkAndClear();

    // prepares data for sim load:
    ctx.values.set(gsi.areaSource.getArea());
    ctx.values.set(gsi.areaSource.getActiveAirport());
    ctx.sdfManager.setParser(AtcId.class, (qq, cc) -> gsi.areaSource.getActiveAirport().getAtcTemplates().select(qqq -> qqq.toAtcId()).getFirst(qqq -> qqq.getName().equals(qq)));
    ctx.values.set(gsi.airplaneTypesSource.getContent());
    ctx.values.set(gsi.fleetsSource.getContent().companyFleets);
    ctx.values.set(gsi.fleetsSource.getContent().gaFleets);
    ctx.values.set("trafficModel", gsi.trafficSource.getContent());
    ctx.values.set(WeatherProvider.class, gsi.weatherSource.getContent());

    SharedAcc sharedContext = new SharedAcc(
            gsi.areaSource.getActiveAirport().getIcao(),
            gsi.areaSource.getActiveAirport().getAtcTemplates().select(qq -> qq.toAtcId()),
            new EDayTimeRun(0),
            new SimulationLog()
    );
    ContextManager.setContext(ISharedAcc.class, sharedContext);

    Simulation simulation = XmlContext.deserialize(root.getChild("simulation"), ctx, Simulation.class);
    simulation.init();

    PostContracts.checkAndClear();

    Game game = new Game(
            gsi.areaSource,
            gsi.airplaneTypesSource,
            gsi.fleetsSource,
            gsi.trafficSource,
            gsi.weatherSource,
            simulation
    );

    return game;
  }

  public void save_old(IGame game, IMap<String, Object> customData, String fileName) {
    XElement root = new XElement("game");

    XmlContext ctx = new XmlContext();
    GameFactoryAndRepository.prepareXmlContextForSimulation(ctx);
    GameFactoryAndRepository.prepareXmlContextForSources(ctx);
    ctx.sdfManager.setSerializer(Game.class, new ObjectSerializer());

    try {
      XmlContext.serialize(root, game, ctx);
    } catch (Exception ex) {
      System.out.println("Failed to save the whole save file");
      ex.printStackTrace(System.out);
    }

    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }
  }

  public void save(IGame game, IMap<String, Object> customData, String fileName) {
    XElement root = new XElement("game");

    XContext ctx = XContext.createSave();
    initSavingContext(ctx);

    try {
      ctx.saver.saveObject(game, root);
    } catch (Exception ex) {
      System.out.println("Failed to save the whole save file");
      ex.printStackTrace(System.out);
    }

    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }
  }

  private void initSavingContext(XContext ctx) {

    ctx.saver.setFormatter(short.class, q -> q.toString());
    ctx.saver.setFormatter(byte.class, q -> q.toString());
    ctx.saver.setFormatter(int.class, q -> q.toString());
    ctx.saver.setFormatter(long.class, q -> q.toString());
    ctx.saver.setFormatter(float.class, q -> q.toString());
    ctx.saver.setFormatter(double.class, q -> q.toString());
    ctx.saver.setFormatter(boolean.class, q -> q.toString());
    ctx.saver.setFormatter(char.class, q -> q.toString());
    ctx.saver.setFormatter(Short.class, q -> q.toString());
    ctx.saver.setFormatter(Byte.class, q -> q.toString());
    ctx.saver.setFormatter(Integer.class, q -> q.toString());
    ctx.saver.setFormatter(Long.class, q -> q.toString());
    ctx.saver.setFormatter(Float.class, q -> q.toString());
    ctx.saver.setFormatter(Double.class, q -> q.toString());
    ctx.saver.setFormatter(Boolean.class, q -> q.toString());
    ctx.saver.setFormatter(Character.class, q -> q.toString());
    ctx.saver.setFormatter(String.class, v -> v);
    ctx.saver.setSerializer(ArrayList.class, getItemsConsumer(true, Object.class, ctx));

    // eSystem
    ctx.saver.setSerializer(AirplaneList.class, getItemsConsumer(false, null, ctx));
    ctx.saver.setSerializer(EList.class, getItemsConsumer(true, Object.class, ctx));
    ctx.saver.setSerializer(EDistinctList.class, getItemsConsumer(true, Object.class, ctx));
    ctx.saver.setSerializer(ESet.class, getItemsConsumer(true, Object.class, ctx));
    ctx.saver.setSerializer(EMap.class, getEntriesConsumer(true, Object.class, Object.class, ctx));
    ctx.saver.setFormatter(Coordinate.class, q -> q.getLatitude().toDecimalString(true) + ";" + q.getLongitude().toDecimalString(true));

    // shared
    ctx.saver.setFormatter(Callsign.class, q -> q.toString(true));
    ctx.saver.setFormatter(Restriction.class, q -> q.direction.toString() + ";" + q.value);
    ctx.saver.setFormatter(EDayTimeStamp.class, v -> v.toDayTimeString());
    ctx.saver.setFormatter(EDayTimeRun.class, v -> v.toDayTimeString());
    ctx.saver.setFormatter(ETimeStamp.class, v->v.toTimeString());
    ctx.saver.setFormatter(Squawk.class, q -> q.toString());

    // area
    ctx.saver.setFormatter(Navaid.class, q -> q.getName());
    ctx.saver.setFormatter(ActiveRunwayThreshold.class, q -> q.getName());

    // airplane type
    ctx.saver.setFormatter(AirplaneType.class, q -> q.name);

    // atc
    ctx.saver.setFormatter(AtcId.class, v -> v.getName());

    // airplane

  }

  private <T extends Iterable<?>> Consumer2<T, XElement> getItemsConsumer(boolean saveItemsType, Class<?> expectedItemType, XContext ctx) {
    Consumer2<T, XElement> ret = (lst, e) -> {
      if (saveItemsType)
        e.setAttribute("__type", lst.getClass().getName());
      ctx.saver.saveItems(lst, expectedItemType, e);
    };

    return ret;
  }

  private <T extends Iterable<Map.Entry<?,?>>> Consumer2<T, XElement> getEntriesConsumer(boolean saveItemsType, Class<?> expectedKeyType, Class<?> expectedValueType, XContext ctx) {
    Consumer2<T, XElement> ret = (lst, e) -> {
      if (saveItemsType)
        e.setAttribute("__type", lst.getClass().getName());
      ctx.saver.saveEntries(lst, expectedKeyType, expectedValueType, e);
    };

    return ret;
  }
}
