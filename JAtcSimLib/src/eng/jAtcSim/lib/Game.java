package eng.jAtcSim.lib;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.sources.*;
import eng.jAtcSim.lib.serialization.LoadSave;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.*;

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
    public Weather initialWeather;
    public WeatherSource.ProviderType weatherProviderType;

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

    System.out.println("* Initialializing weather");
    g.weatherSource = new WeatherSource(
        gsi.weatherProviderType,
        g.areaXmlSource.getActiveAirport().getIcao(),
        gsi.initialWeather);

    System.out.println("* Generating traffic");
    if (gsi.specificTraffic != null)
      g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.specificTraffic, 0);
    else
      g.trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.activeAirportTraffic, 0);

    System.out.println("* Creating simulation");
    g.simulation = new Simulation(
        g.areaXmlSource.getContent(), g.airplaneTypesXmlSource.getContent(), g.fleetsXmlSource.getContent(), g.trafficXmlSource.getActiveTraffic(),
        g.areaXmlSource.getActiveAirport(),
        g.weatherSource.getContent(), gsi.startTime,
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
    LoadSave.loadField(root, ret, "weatherSource");

    ret.areaXmlSource.load();
    ret.areaXmlSource.init(ret.areaXmlSource.getActiveAirportIndex());

    ret.airplaneTypesXmlSource.load();
    ret.airplaneTypesXmlSource.init();

    ret.fleetsXmlSource.load();
    ret.fleetsXmlSource.init(ret.airplaneTypesXmlSource.getContent());

    IList<Traffic> loadedSpecificTraffic = ret.trafficXmlSource.getSpecificTraffic();
    ret.trafficXmlSource.load();
    ret.trafficXmlSource.init(ret.areaXmlSource.getActiveAirport(), loadedSpecificTraffic.toArray(Traffic.class));

    Simulation sim = new Simulation(
        ret.areaXmlSource.getContent(), ret.airplaneTypesXmlSource.getContent(),
        ret.fleetsXmlSource.getContent(), ret.trafficXmlSource.getActiveTraffic(),
        ret.areaXmlSource.getActiveAirport(),
        ret.weatherSource.getContent(), new ETime(0), 0, 0);
    sim.init();

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
