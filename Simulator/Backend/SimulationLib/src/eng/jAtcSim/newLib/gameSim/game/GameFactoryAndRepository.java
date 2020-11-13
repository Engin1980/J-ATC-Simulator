package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneXmlContextInit;
import eng.jAtcSim.newLib.area.AreaXmlContextInit;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.messaging.MessagingXmlContextInit;
import eng.jAtcSim.newLib.mood.MoodXmlContextInit;
import eng.jAtcSim.newLib.shared.GID;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSim.newLib.traffic.TrafficXmlContextInit;
import eng.newXmlUtils.SDFFactory;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.implementations.ObjectSerializer;

public class GameFactoryAndRepository {
  public Game create(GameStartupInfo gsi) {
    Game game;
    ApplicationLog appLog = Context.getApp().getAppLog();

    AreaSource areaSource;
    AirplaneTypesSource airplaneTypesSource;
    FleetsSource fleetsSource;
    TrafficSource trafficSource;
    WeatherSource weatherSource;
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
//TODEL if not required
      //      if (gsi.trafficSource.specificTraffic != null) {
//        trafficSource = new TrafficUserSource(gsi.trafficSource.specificTraffic);
//      } else {
//        trafficSource = new TrafficXmlSource(gsi.trafficSource.trafficXmlFile);
//      }
      EAssert.isNotNull(gsi.trafficSource, "Traffic-Source not set.");
      gsi.trafficSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      appLog.write(ApplicationLog.eType.info, "Initializing weather");
      //TODEL
//      switch (gsi.weatherSource.weatherProviderType) {
//        case online:
//          weatherSource = new WeatherOnlineSource(true, gsi.areaSource.icao, gsi.weatherSource.initialWeather);
//          break;
//        case xml:
//          weatherSource = new WeatherXmlSource(gsi.weatherSource.weatherXmlFile);
//          break;
//        case user:
//          weatherSource = new WeatherUserSource(gsi.weatherSource.initialWeather);
//          break;
//        default:
//          throw new EEnumValueUnsupportedException(gsi.weatherSource.weatherProviderType);
//      }
      EAssert.isNotNull(gsi.weatherSource, "Weather-Source not set.");
      gsi.weatherSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load, download or initialize weather.", ex);
    }

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
    return game;
  }

  public Game load(String fileName) {
    //TODO update
    IMap<String, Object> customData = new EMap<>();

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();

    XmlContext ctx = new XmlContext();
    prepareXmlContext(ctx);

    Game ret = XmlContext.deserialize(root, ctx, Game.class);
    return ret;
  }

  private static void prepareXmlContext(XmlContext ctx) {
    ctx.sdfManager.setSerializer(GID.class, new ObjectSerializer());
    ctx.sdfManager.setSerializers(SDFFactory.getSimpleSerializers());
    ctx.sdfManager.setSerializers(SDFFactory.getSimpleArraySerializers());
    ctx.sdfManager.setSerializers(SDFFactory.getESystemSerializers());
    ctx.sdfManager.setSerializers(SharedXmlUtils.Serializers.serializers);

    AreaXmlContextInit.prepareXmlContext(ctx);
    TrafficXmlContextInit.prepareXmlContext(ctx);
    MessagingXmlContextInit.prepareXmlContext(ctx);
    MoodXmlContextInit.prepareXmlContext(ctx);
    AirplaneXmlContextInit.prepareXmlContext(ctx);

    Simulation.prepareXmlContext(ctx);
    Game.prepareXmlContext(ctx);
  }

  public void save(IGame game, IMap<String, Object> customData, String fileName) {
    XElement root = new XElement("game");

    XmlContext ctx = new XmlContext();
    GameFactoryAndRepository.prepareXmlContext(ctx);

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
}
