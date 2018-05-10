package eng.jAtcSim.lib;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.sources.AirplaneTypesXmlSource;
import eng.jAtcSim.lib.global.sources.AreaXmlSource;
import eng.jAtcSim.lib.global.sources.FleetsXmlSource;
import eng.jAtcSim.lib.global.sources.TrafficXmlSource;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.DynamicWeatherProvider;
import eng.jAtcSim.lib.weathers.NoaaDynamicWeatherProvider;
import eng.jAtcSim.lib.weathers.StaticWeatherProvider;
import eng.jAtcSim.lib.weathers.WeatherProvider;

import java.util.Calendar;

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
    private WeatherProvider weatherProvider;
  }

  private AreaXmlSource areaXmlSource;
  private AirplaneTypesXmlSource airplaneTypesXmlSource;
  private FleetsXmlSource fleetsXmlSource;
  private TrafficXmlSource trafficXmlSource;
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

    System.out.println("* Generating traffic");
    if (gsi.specificTraffic != null)
      g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.specificTraffic, 0);
    else
      g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.activeAirportTraffic, 0);

    g.simulation = new Simulation(
        g.areaXmlSource.getContent(), g.airplaneTypesXmlSource.getContent(), g.fleetsXmlSource.getContent(), g.trafficXmlSource.getActiveTraffic(),
        g.areaXmlSource.getActiveAirport(),
        gsi.weatherProvider, gsi.startTime,
        gsi.secondLengthInMs,
        gsi.emergencyPerDayProbability);
    g.simulation.init();

    return g;
  }

  public static Game load(String fileName) {
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

    ret.areaXmlSource.load();
    ret.areaXmlSource.init(ret.areaXmlSource.getActiveAirportIndex());

    ret.airplaneTypesXmlSource.load();
    ret.airplaneTypesXmlSource.init();

    ret.fleetsXmlSource.load();
    ret.fleetsXmlSource.init(ret.airplaneTypesXmlSource.getContent());

    IList<Traffic> loadedSpecificTraffic = ret.trafficXmlSource.getSpecificTraffic();
    ret.trafficXmlSource.load();
    ret.trafficXmlSource.init(ret.areaXmlSource.getActiveAirport(), loadedSpecificTraffic.toArray(Traffic.class));

    Simulation sim = new Simulation(bubla dopsat parametry);

    XElement tmp = root.getChild("simulation");
    sim.load(tmp);

    return ret;
  }

  public void save(String fileName) {
    XElement root = new XElement("game");

    LoadSave.saveField(root, this, "areaXmlSource");
    LoadSave.saveField(root, this, "airplaneTypesXmlSource");
    LoadSave.saveField(root, this, "fleetsXmlSource");
    LoadSave.saveField(root, this, "trafficXmlSource");

    {
      XElement tmp = new XElement("simulation");
      simulation.save(tmp);
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
