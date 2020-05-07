package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationContext;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public class Game {
  public static Game create(GameStartupInfo gsi) {
    Game game = new Game();
    ApplicationLog appLog = SharedAcc.getAppLog();

    try {
      appLog.writeLine(ApplicationLog.eType.info, "Loading area");
      game.areaSource = new AreaSource(gsi.areaSource.areaXmlFile, gsi.areaSource.icao);
      game.areaSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize area.", ex);
    }
    try {
      appLog.writeLine(ApplicationLog.eType.info, "Loading plane types");
      game.airplaneTypesSource = new AirplaneTypesSource(gsi.planesXmlFile);
      game.airplaneTypesSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize plane types.", ex);
    }

    try {
      appLog.writeLine(ApplicationLog.eType.info, "Loading fleets");
      game.fleetsSource = new FleetsSource(gsi.generalAviationFleetsXmlFile, gsi.companyFleetsXmlFile);
      game.fleetsSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize fleets.", ex);
    }

    try {
      appLog.writeLine(ApplicationLog.eType.info, "Loading traffic");
      if (gsi.trafficSource.specificTraffic != null) {
        game.trafficSource = new TrafficUserSource(gsi.trafficSource.specificTraffic);
      } else {
        game.trafficSource = new TrafficXmlSource(gsi.trafficSource.trafficXmlFile);
      }
      game.trafficSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      appLog.writeLine(ApplicationLog.eType.info, "Initializing weather");
      switch (gsi.weatherSource.weatherProviderType) {
        case online:
          game.weatherSource = new WeatherOnlineSource(true, gsi.areaSource.icao, gsi.weatherSource.initialWeather);
          break;
        case xml:
          game.weatherSource = new WeatherXmlSource(gsi.weatherSource.weatherXmlFile);
          break;
        case user:
          game.weatherSource = new WeatherUserSource(gsi.weatherSource.initialWeather);
          break;
        default:
          throw new EEnumValueUnsupportedException(gsi.weatherSource.weatherProviderType);
      }
      game.weatherSource.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to load, download or initialize weather.", ex);
    }

//    TrafficManager.TrafficManagerSettings tms;
//    try {
//      tms = new TrafficManager.TrafficManagerSettings(
//          gsi.allowTrafficDelays, gsi.maxTrafficPlanes, gsi.trafficDensityPercentage);
//    } catch (Exception ex){
//      throw new EApplicationException("Unable to initialize the traffic manager.", ex);
//    }

    try {
      appLog.writeLine(ApplicationLog.eType.info, "Creating the simulation");
      SimulationContext simulationContext = new SimulationContext(
          game.areaSource.getContent(),
          game.areaSource.getIcao(),
          game.airplaneTypesSource.getContent(),
          game.fleetsSource.getContent().companyFleets,
          game.fleetsSource.getContent().gaFleets,
          game.trafficSource.getContent(),
          game.weatherSource.getContent()
      );
      SimulationSettings simulationSettings = new SimulationSettings(
          null, null,
          gsi.trafficSettings,
          gsi.simulationSettings
      );

      game.simulation = new Simulation(simulationContext, simulationSettings);
      appLog.writeLine(ApplicationLog.eType.info, "Initializing the simulation");
      game.simulation.init();
    } catch (Exception ex) {
      throw new EApplicationException("Unable to create or initialize the simulation.", ex);
    }
    return game;
  }
  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

//  public static Game load(String fileName, IMap<String, Object> customData) {
//    eng.jAtcSim.newLib.gameSim.Game ret = new eng.jAtcSim.newLib.gameSim.Game();
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading xml document...");
//    XDocument doc;
//    try {
//      doc = XDocument.load(fileName);
//    } catch (EXmlException e) {
//      throw new EApplicationException("Unable to load xml document.", e);
//    }
//
//    XElement root = doc.getRoot();
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading area...");
//    LoadSave.loadField(root, ret, "areaSource");
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading airplane types...");
//    LoadSave.loadField(root, ret, "airplaneTypesSource");
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading fleets...");
//    LoadSave.loadField(root, ret, "fleetsSource");
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading traffic...");
//    LoadSave.loadField(root, ret, "trafficSource");
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading weather...");
//    LoadSave.loadField(root, ret, "weatherSource");
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing area...");
//    ret.areaSource.init();
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing airplane types...");
//    ret.airplaneTypesSource.init();
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing fleets...");
//    ret.fleetsSource.init(ret.airplaneTypesSource.getContent());
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing traffic...");
//    ret.trafficSource.init();
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing weather...");
//    ret.weatherSource.init();
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Creating the simulation...");
//    ret.simulation = new Simulation(
//        ret.areaSource.getContent(), ret.airplaneTypesSource.getContent(),
//        ret.fleetsSource.getContent(), ret.trafficSource.getContent(),
//        ret.areaSource.getActiveAirport(),
//        ret.weatherSource.getContent(), new ETime(0), 0, 0,
//        new TrafficManager.TrafficManagerSettings(false, 0, 0), 5);
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing the simulation...");
//    ret.simulation.init();
//
//    XElement tmp = root.getChild("simulation");
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading the simulation (may take a while)...");
//    ret.simulation.load(tmp);
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading radar shortcuts...");
//    {
//      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
//      ret.simulation.setCommandShortcuts(shortcuts);
//    }
//
//    Acc.log().writeLine(ApplicationLog.eType.info, "Loading custom data...");
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
//  }
//
//  public void save(String fileName, IMap<String, Object> customData) {
//    long saveStart = System.currentTimeMillis();
//    XElement root = new XElement("game");
//
//    LoadSave.saveField(root, this, "areaSource");
//    LoadSave.saveField(root, this, "airplaneTypesSource");
//    LoadSave.saveField(root, this, "fleetsSource");
//    LoadSave.saveField(root, this, "trafficSource");
//    LoadSave.saveField(root, this, "weatherSource");
//
//    {
//      XElement tmp = new XElement("simulation");
//      simulation.save(tmp);
//      root.addElement(tmp);
//    }
//
//    {
//      XElement tmp = new XElement("simulation");
//      LoadSave.saveAsElement(root, "shortcuts", simulation.getCommandShortcuts());
//    }
//
//    {
//      XElement tmp = new XElement("custom");
//      for (Map.Entry<String, Object> entry : customData) {
//        try {
//          LoadSave.saveAsElement(tmp, entry.getKey(), entry.getValue());
//        } catch (Exception ex) {
//          throw new ERuntimeException("Failed to save custom data to game. Data key: " + entry.getKey() + ", data value: " + entry.getValue(), ex);
//        }
//      }
//      root.addElement(tmp);
//    }
//
//    long saveIn= System.currentTimeMillis();
//
//    XDocument doc = new XDocument(root);
//    try {
//      doc.save(fileName);
//    } catch (EXmlException e) {
//      throw new EApplicationException("Failed to save simulation.", e);
//    }
//
//    long saveEnd = System.currentTimeMillis();
//
//    System.out.println("## save output:");
//    System.out.println("## storing: " + (saveIn - saveStart));
//    System.out.println("## writing: " + (saveEnd - saveIn));
//  }

//  public Simulation getSimulation() {
//    return simulation;
//  }
}
