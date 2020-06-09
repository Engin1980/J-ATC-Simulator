package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.IMap;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.game.startupInfos.GameStartupInfo;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationContext;
import eng.jAtcSim.newLib.gameSim.simulation.SimulationSettings;
import eng.jAtcSim.newLib.shared.context.SharedAcc;
import eng.jAtcSim.newLib.shared.logging.ApplicationLog;

public class Game implements IGame {
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

  @Override
  public void save(String toString, IMap<String, Object> tmp) {
    //TODO Implement this: Implement saving
    throw new ToDoException("Implement saving");
  }

//  public static Game load(String fileName, IMap<String, Object> customData) {
//    eng.jAtcSim.newLib.gameSim.Game ret = new eng.jAtcSim.newLib.gameSim.Game();
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading xml document...");
//    XDocument doc;
//    try {
//      doc = XDocument.load(fileName);
//    } catch (EXmlException e) {
//      throw new EApplicationException("Unable to load xml document.", e);
//    }
//
//    XElement root = doc.getRoot();
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading area...");
//    LoadSave.loadField(root, ret, "areaSource");
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading airplane types...");
//    LoadSave.loadField(root, ret, "airplaneTypesSource");
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading fleets...");
//    LoadSave.loadField(root, ret, "fleetsSource");
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading traffic...");
//    LoadSave.loadField(root, ret, "trafficSource");
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading weather...");
//    LoadSave.loadField(root, ret, "weatherSource");
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing area...");
//    ret.areaSource.init();
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing airplane types...");
//    ret.airplaneTypesSource.init();
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing fleets...");
//    ret.fleetsSource.init(ret.airplaneTypesSource.getContent());
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing traffic...");
//    ret.trafficSource.init();
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing weather...");
//    ret.weatherSource.init();
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Creating the simulation...");
//    ret.simulation = new Simulation(
//        ret.areaSource.getContent(), ret.airplaneTypesSource.getContent(),
//        ret.fleetsSource.getContent(), ret.trafficSource.getContent(),
//        ret.areaSource.getActiveAirport(),
//        ret.weatherSource.getContent(), new ETime(0), 0, 0,
//        new TrafficManager.TrafficManagerSettings(false, 0, 0), 5);
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Initializing the simulation...");
//    ret.simulation.init();
//
//    XElement tmp = root.getChild("simulation");
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading the simulation (may take a while)...");
//    ret.simulation.load(tmp);
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading radar shortcuts...");
//    {
//      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
//      ret.simulation.setCommandShortcuts(shortcuts);
//    }
//
//    SharedAcc.getAppLog().writeLine(ApplicationLog.eType.info, "Loading custom data...");
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
