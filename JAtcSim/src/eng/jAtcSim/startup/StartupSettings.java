package eng.jAtcSim.startup;

import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;

public class StartupSettings {


  public static class Files {
    public String trafficXmlFile;
    public String planesXmlFile;
    public String areaXmlFile;
    public String fleetsXmlFile;
  }

  public static class Recent {
    public String time; // TODO time should be local-time
    public String icao;
  }

  public static class Weather {
    public int userChanges;
    public boolean useOnline;
    public String metar;
  }

  public static class Traffic {
    public enum eTrafficType {
      custom,
      xml,
      airportDefined
    }

    public eTrafficType type;
    public String trafficAirportDefinedTitle;
    public CustomTraffic customTraffic = new CustomTraffic();
    public int maxPlanes;
    public double densityPercentage;
    public boolean allowDelays;
  }

  public static class CustomTraffic {

    public boolean useExtendedCallsigns;
    public int weightTypeD;
    public int weightTypeC;
    public int weightTypeB;
    public int weightTypeA;
    public int arrivals2departuresRatio;
    public int movementsPerHour;

  }

  public static class Radar {
    public String packClass = "Pack";
  }

  public static class Simulation {
    public int secondLengthInMs = 1000;
    public double emergencyPerDayProbability = 1 / 7d;
  }

  public class Loaded {
    private Area area = null;
    private AirplaneTypes types = null;
    private Fleets fleets = null;

    public Area tryGetArea() {
      if (area == null)
        try {
          area = XmlLoadHelper.loadNewArea(files.areaXmlFile);
        } catch (Exception ex) {
          area = null;
        }
      return area;
    }

    public AirplaneTypes tryGetTypes() {
      if (types == null)
        try {
          types = XmlLoadHelper.loadPlaneTypes(files.planesXmlFile);
        } catch (Exception ex) {
          types = null;
        }
      return types;
    }

    public Fleets tryGetFleets() {
      if (fleets == null)
        try {
          fleets = XmlLoadHelper.loadFleets(files.fleetsXmlFile);
        } catch (Exception ex) {
          fleets = null;
        }

      return fleets;
    }

    public void reset() {
      area = null;
      types = null;
      fleets = null;
    }
  }

  @XmlIgnore
  public final Loaded Loaded = this.new Loaded();
  public Files files;
  public Recent recent;
  public Weather weather;
  public Traffic traffic;
  public Radar radar;
  public Simulation simulation;

  public StartupSettings() {
    this.files = new Files();
    this.recent = new Recent();
    this.weather = new Weather();
    this.traffic = new Traffic();
    this.simulation = new Simulation();
  }
}
