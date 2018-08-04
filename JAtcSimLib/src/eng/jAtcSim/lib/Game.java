package eng.jAtcSim.lib;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.sources.*;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.TrafficManager;
import eng.jAtcSim.lib.weathers.Weather;

import java.util.Map;

public class Game {

  public static class GameStartupInfo {
    public String areaXmlFile;
    public String planesXmlFile;
    public String fleetsXmlFile;
    public String trafficXmlFile;
    public String icao;
    public Traffic specificTraffic;
    public ETime startTime;
    public int secondLengthInMs;
    public double emergencyPerDayProbability;
    public Weather initialWeather;
    public WeatherSource.ProviderType weatherProviderType;

    public TrafficXmlSource.TrafficSource trafficSourceType;
    public String lookForTrafficTitle;
    public boolean allowTrafficDelays;
    public int maxTrafficPlanes;
    public double trafficDensityPercentage;
  }

  private AreaXmlSource areaXmlSource;
  private AirplaneTypesXmlSource airplaneTypesXmlSource;
  private FleetsXmlSource fleetsXmlSource;
  private TrafficXmlSource trafficXmlSource;
  private WeatherSource weatherSource;
  private Simulation simulation;

  public static Game create(GameStartupInfo gsi) {
    Game g = new Game();

    System.out.println("* Loading area");
    g.areaXmlSource = new AreaXmlSource(gsi.areaXmlFile);
    g.areaXmlSource.load();
    g.areaXmlSource.init(gsi.icao);

    System.out.println("* Loading plane types");
    g.airplaneTypesXmlSource = new AirplaneTypesXmlSource(gsi.planesXmlFile);
    g.airplaneTypesXmlSource.load();
    g.airplaneTypesXmlSource.init();

    System.out.println("* Loading fleets");
    g.fleetsXmlSource = new FleetsXmlSource(gsi.fleetsXmlFile);
    g.fleetsXmlSource.load();
    g.fleetsXmlSource.init(g.airplaneTypesXmlSource.getContent());

    System.out.println("* Loading traffic");
    g.trafficXmlSource = new TrafficXmlSource(gsi.trafficXmlFile);
    g.trafficXmlSource.load();
    g.trafficXmlSource.init(g.areaXmlSource.getActiveAirport(), gsi.specificTraffic);

    System.out.println("* Initializing weather");
    g.weatherSource = new WeatherSource(
        gsi.weatherProviderType,
        g.areaXmlSource.getActiveAirport().getIcao());
    g.weatherSource.init(gsi.initialWeather);

    System.out.println("* Generating traffic");
    switch (gsi.trafficSourceType){
      case activeAirportTraffic:
        g.trafficXmlSource.setActiveTraffic(gsi.trafficSourceType,gsi.lookForTrafficTitle);
        break;
      case xmlFileTraffic:
        g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.xmlFileTraffic, gsi.lookForTrafficTitle );
        break;
      case specificTraffic:
        g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.specificTraffic, null);
    }

    TrafficManager.TrafficManagerSettings tms = new TrafficManager.TrafficManagerSettings(
        gsi.allowTrafficDelays, gsi.maxTrafficPlanes, gsi.trafficDensityPercentage);

    System.out.println("* Creating simulation");
    g.simulation = new Simulation(
        g.areaXmlSource.getContent(), g.airplaneTypesXmlSource.getContent(), g.fleetsXmlSource.getContent(), g.trafficXmlSource.getActiveTraffic(),
        g.areaXmlSource.getActiveAirport(),
        g.weatherSource.getContent(), gsi.startTime,
        gsi.secondLengthInMs,
        gsi.emergencyPerDayProbability,
        tms);
    g.simulation.init();

    return g;
  }

  public static Game load(String fileName, IMap<String, Object> customData) {
    Game ret = new Game();

    XDocument doc;
    try {
      doc = XDocument.load(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Unable to load xml document.", e);
    }

    XElement root = doc.getRoot();

    LoadSave.loadField(root, ret, "areaXmlSource");
    LoadSave.loadField(root, ret, "airplaneTypesXmlSource");
    LoadSave.loadField(root, ret, "fleetsXmlSource");
    LoadSave.loadField(root, ret, "trafficXmlSource");
    LoadSave.loadField(root, ret, "weatherSource");

    ret.areaXmlSource.load();
    ret.areaXmlSource.init(ret.areaXmlSource.getActiveAirportIndex());

    ret.airplaneTypesXmlSource.load();
    ret.airplaneTypesXmlSource.init();

    ret.fleetsXmlSource.load();
    ret.fleetsXmlSource.init(ret.airplaneTypesXmlSource.getContent());

    Traffic loadedSpecificTraffic = ret.trafficXmlSource.getSpecificTraffic();
    ret.trafficXmlSource.load();
    ret.trafficXmlSource.init(ret.areaXmlSource.getActiveAirport(), loadedSpecificTraffic);

    ret.weatherSource.init(ret.weatherSource.getWeather());

    ret.simulation = new Simulation(
        ret.areaXmlSource.getContent(), ret.airplaneTypesXmlSource.getContent(),
        ret.fleetsXmlSource.getContent(), ret.trafficXmlSource.getActiveTraffic(),
        ret.areaXmlSource.getActiveAirport(),
        ret.weatherSource.getContent(), new ETime(0), 0, 0,
        new TrafficManager.TrafficManagerSettings(false, 0, 0));
    ret.simulation.init();

    XElement tmp = root.getChild("simulation");
    ret.simulation.load(tmp);

    {
      IMap<String, String> shortcuts = (IMap<String, String>) LoadSave.loadFromElement(root, "shortcuts", IMap.class);
      ret.simulation.setCommandShortcuts(shortcuts);
    }

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
    XElement root = new XElement("game");

    LoadSave.saveField(root, this, "areaXmlSource");
    LoadSave.saveField(root, this, "airplaneTypesXmlSource");
    LoadSave.saveField(root, this, "fleetsXmlSource");
    LoadSave.saveField(root, this, "trafficXmlSource");
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

    XDocument doc = new XDocument(root);
    try {
      doc.save(fileName);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to save simulation.", e);
    }

  }

  public Simulation getSimulation() {
    return simulation;
  }
}
