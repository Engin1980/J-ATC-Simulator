package eng.jAtcSim.newLib.gameSim;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.newLib.area.serialization.LoadSave;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.global.logging.ApplicationLog;
import eng.jAtcSim.newLib.global.newSources.*;
import eng.jAtcSim.newLib.traffic.TrafficProvider;
import eng.jAtcSim.newLib.traffic.TrafficManager;
import eng.jAtcSim.newLib.weathers.Weather;

import java.util.Map;

public class Game {

  public static class GameStartupInfo {

    public enum SourceType {
      xml,
      user
    }
    public enum WeatherSourceType{
      xml,
      user,
      online
    }

    public String areaXmlFile;
    public String planesXmlFile;
    public String fleetsXmlFile;
    public String trafficXmlFile;
    public String weatherXmlFile;
    public String icao;
    public TrafficProvider specificTraffic;
    public ETime startTime;
    public int secondLengthInMs;
    public double emergencyPerDayProbability;
    public Weather initialWeather;
    public WeatherSourceType weatherProviderType;
    public SourceType trafficSourceType;
    public boolean allowTrafficDelays;
    public int maxTrafficPlanes;
    public double trafficDensityPercentage;
    public int statsSnapshotDistanceInMinutes;
  }

  private AreaSource areaSource;
  private AirplaneTypesSource airplaneTypesSource;
  private FleetsSource fleetsSource;
  private TrafficSource trafficSource;
  private eng.jAtcSim.newLib.global.newSources.WeatherSource weatherSource;
  private Simulation simulation;

  public static Game create(GameStartupInfo gsi) {
    Game g = new Game();

    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Loading area");
      g.areaSource = new AreaSource(gsi.areaXmlFile, gsi.icao);
      g.areaSource.init();
    } catch (Exception ex){
      throw new EApplicationException("Unable to load or initialize area.", ex);
    }
    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Loading plane types");
      g.airplaneTypesSource = new AirplaneTypesSource(gsi.planesXmlFile);
      g.airplaneTypesSource.init();
    } catch (Exception ex){
      throw new EApplicationException("Unable to load or initialize plane types.", ex);
    }

    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Loading fleets");
      g.fleetsSource = new FleetsSource(gsi.fleetsXmlFile);
      g.fleetsSource.init(g.airplaneTypesSource.getContent());
    } catch (Exception ex){
      throw new EApplicationException("Unable to load or initialize fleets.", ex);
    }

    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Loading traffic");
      switch (gsi.trafficSourceType) {
        case user:
          g.trafficSource = new UserTrafficSource(gsi.specificTraffic);
          break;
        case xml:
          g.trafficSource = new XmlTrafficSource(gsi.trafficXmlFile);
          break;
        default:
          throw new EEnumValueUnsupportedException(gsi.specificTraffic);
      }
      g.trafficSource.init();
    } catch (Exception ex){
      throw new EApplicationException("Unable to load or initialize traffic.", ex);
    }

    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Initializing weather");
      switch (gsi.weatherProviderType) {
        case online:
          g.weatherSource = new OnlineWeatherSource(true, gsi.icao, gsi.initialWeather);
          break;
        case xml:
          g.weatherSource = new XmlWeatherSource(gsi.weatherXmlFile);
          break;
        case user:
          g.weatherSource = new UserWeatherSource(gsi.initialWeather);
          break;
        default:
          throw new EEnumValueUnsupportedException(gsi.weatherProviderType);
      }
      g.weatherSource.init();
    } catch ( Exception ex){
      throw new EApplicationException("Unable to load, download or initialize weather.",ex);
    }

    TrafficManager.TrafficManagerSettings tms;
    try {
      tms = new TrafficManager.TrafficManagerSettings(
          gsi.allowTrafficDelays, gsi.maxTrafficPlanes, gsi.trafficDensityPercentage);
    } catch (Exception ex){
      throw new EApplicationException("Unable to initialize the traffic manager.", ex);
    }

    try {
      Acc.log().writeLine(ApplicationLog.eType.info, "Creating the simulation");
      g.simulation = new Simulation(
          g.areaSource.getContent(), g.airplaneTypesSource.getContent(), g.fleetsSource.getContent(), g.trafficSource.getContent(),
          g.areaSource.getActiveAirport(),
          g.weatherSource.getContent(),
          gsi.startTime,
          gsi.secondLengthInMs,
          gsi.emergencyPerDayProbability,
          tms, gsi.statsSnapshotDistanceInMinutes);
      Acc.log().writeLine(ApplicationLog.eType.info, "Initializing the simulation");
      g.simulation.init();
    } catch (Exception ex){
      throw new EApplicationException("Unable to create or initialize the simulation.", ex);
    }
    return g;
  }

  public static Game load(String fileName, IMap<String, Object> customData) {
    Game ret = new Game();

    Acc.log().writeLine(ApplicationLog.eType.info, "Loading xml document...");
    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();

    Acc.log().writeLine(ApplicationLog.eType.info, "Loading area...");
    LoadSave.loadField(root, ret, "areaSource");
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading airplane types...");
    LoadSave.loadField(root, ret, "airplaneTypesSource");
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading fleets...");
    LoadSave.loadField(root, ret, "fleetsSource");
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading traffic...");
    LoadSave.loadField(root, ret, "trafficSource");
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading weather...");
    LoadSave.loadField(root, ret, "weatherSource");

    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing area...");
    ret.areaSource.init();
    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing airplane types...");
    ret.airplaneTypesSource.init();
    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing fleets...");
    ret.fleetsSource.init(ret.airplaneTypesSource.getContent());
    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing traffic...");
    ret.trafficSource.init();
    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing weather...");
    ret.weatherSource.init();

    Acc.log().writeLine(ApplicationLog.eType.info, "Creating the simulation...");
    ret.simulation = new Simulation(
        ret.areaSource.getContent(), ret.airplaneTypesSource.getContent(),
        ret.fleetsSource.getContent(), ret.trafficSource.getContent(),
        ret.areaSource.getActiveAirport(),
        ret.weatherSource.getContent(), new ETime(0), 0, 0,
        new TrafficManager.TrafficManagerSettings(false, 0, 0), 5);

    Acc.log().writeLine(ApplicationLog.eType.info, "Initializing the simulation...");
    ret.simulation.init();

    XElement tmp = root.getChild("simulation");
    Acc.log().writeLine(ApplicationLog.eType.info, "Loading the simulation (may take a while)...");
    ret.simulation.load(tmp);

    Acc.log().writeLine(ApplicationLog.eType.info, "Loading radar shortcuts...");
    {
      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
      ret.simulation.setCommandShortcuts(shortcuts);
    }

    Acc.log().writeLine(ApplicationLog.eType.info, "Loading custom data...");
    {
      XElement elm = root.getChild("custom");
      for (XElement child : elm.getChildren()) {
        String key = child.getName();
        Object obj = LoadSave.loadFromElement(elm, key, Object.class);
        customData.set(key, obj);
      }
    }

    return ret;
  }

  public void save(String fileName, IMap<String, Object> customData) {
    long saveStart = System.currentTimeMillis();
    XElement root = new XElement("game");

    LoadSave.saveField(root, this, "areaSource");
    LoadSave.saveField(root, this, "airplaneTypesSource");
    LoadSave.saveField(root, this, "fleetsSource");
    LoadSave.saveField(root, this, "trafficSource");
    LoadSave.saveField(root, this, "weatherSource");

    {
      XElement tmp = new XElement("simulation");
      simulation.save(tmp);
      root.addElement(tmp);
    }

    {
      XElement tmp = new XElement("simulation");
      LoadSave.saveAsElement(root, "shortcuts", simulation.getCommandShortcuts());
    }

    {
      XElement tmp = new XElement("custom");
      for (Map.Entry<String, Object> entry : customData) {
        try {
          LoadSave.saveAsElement(tmp, entry.getKey(), entry.getValue());
        } catch (Exception ex) {
          throw new ERuntimeException("Failed to save custom data to game. Data key: " + entry.getKey() + ", data value: " + entry.getValue(), ex);
        }
      }
      root.addElement(tmp);
    }

    long saveIn= System.currentTimeMillis();

    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }

    long saveEnd = System.currentTimeMillis();

    System.out.println("## save output:");
    System.out.println("## storing: " + (saveIn - saveStart));
    System.out.println("## writing: " + (saveEnd - saveIn));
  }

  public Simulation getSimulation() {
    return simulation;
  }
}

