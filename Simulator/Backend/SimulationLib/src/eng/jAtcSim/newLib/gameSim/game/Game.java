package eng.jAtcSim.newLib.gameSim.game;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.gameSim.simulation.Simulation;
import eng.jAtcSim.newLib.gameSim.xml.WeatherSourceSerializer;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.serializers.EntriesViaStringSerializer;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

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


//  public static Game load(String fileName, IMap<String, Object> customData) {
//    eng.jAtcSim.newLib.gameSim.Game ret = new eng.jAtcSim.newLib.gameSim.Game();
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading xml document...");
//    XDocument doc;
//    try {
//      doc = XDocument.load(fileName);
//    } catch (EXmlException e) {
//      throw new EApplicationException("Unable to load xml document.", e);
//    }
//
//    XElement root = doc.getRoot();
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading area...");
//    LoadSave.loadField(root, ret, "areaSource");
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading airplane types...");
//    LoadSave.loadField(root, ret, "airplaneTypesSource");
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading fleets...");
//    LoadSave.loadField(root, ret, "fleetsSource");
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading traffic...");
//    LoadSave.loadField(root, ret, "trafficSource");
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading weather...");
//    LoadSave.loadField(root, ret, "weatherSource");
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing area...");
//    ret.areaSource.init();
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing airplane types...");
//    ret.airplaneTypesSource.init();
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing fleets...");
//    ret.fleetsSource.init(ret.airplaneTypesSource.getContent());
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing traffic...");
//    ret.trafficSource.init();
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing weather...");
//    ret.weatherSource.init();
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Creating the simulation...");
//    ret.simulation = new Simulation(
//        ret.areaSource.getContent(), ret.airplaneTypesSource.getContent(),
//        ret.fleetsSource.getContent(), ret.trafficSource.getContent(),
//        ret.areaSource.getActiveAirport(),
//        ret.weatherSource.getContent(), new ETime(0), 0, 0,
//        new TrafficManager.TrafficManagerSettings(false, 0, 0), 5);
//
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Initializing the simulation...");
//    ret.simulation.init();
//
//    XElement tmp = root.getChild("simulation");
//    Context.getShared().getAppLog().writeLine(ApplicationLog.eType.info, "Loading the simulation (may take a while)...");
//    ret.simulation.load(tmp);
//
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
//  }
//


  @Override
  public void save(String fileName, IMap<String, String> customData) {
    XElement root = new XElement("game");

    XmlSaveUtils.saveIntoElementChild(root, "areaSource", this.areaSource,
            q -> sf("%s;%s", q.getFileName(), q.getIcao()));
    XmlSaveUtils.saveIntoElementChild(root, "airplaneTypesSource", this.airplaneTypesSource,
            q -> q.getFileName());
    XmlSaveUtils.saveIntoElementChild(root, "fleetsSource", this.fleetsSource,
            q -> sf("%s;%s", q.getCompanyFileName(), q.getGeneralAviationFileName()));
    XmlSaveUtils.saveIntoElementChild(root, "trafficSource", this.trafficSource,
            q -> ((TrafficXmlSource) q).getFileName());
    XmlSaveUtils.saveIntoElementChild(root, "weatherSource", this.weatherSource, new WeatherSourceSerializer());

    {
      XElement tmp = new XElement("simulation");
      this.simulation.save(tmp);
      root.addElement(tmp);
    }
//
//    XmlSaveUtils.saveIntoElementChild(root, "customData", customData,
//            new EntriesViaStringSerializer<>(q -> q, q -> q));


    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }
  }
}
