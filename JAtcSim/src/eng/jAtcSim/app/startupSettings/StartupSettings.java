package eng.jAtcSim.app.startupSettings;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.world.Area;

import java.time.LocalTime;

public class StartupSettings {


  public static class Files {
    public String trafficXmlFile;
    public String planesXmlFile;
    public String areaXmlFile;
    public String fleetsXmlFile;
  }

  public static class Recent {
    public LocalTime time;
    public String icao;
  }

  public static class Weather {
    public boolean useOnline;
    public int windDirection;
    public int windSpeed;
    public int cloudBaseAltitudeFt;
    public double cloudBaseProbability;
    public int visibilityInM;
  }

  public static class Traffic {
    public enum eTrafficType {
      custom,
      xml,
      airportDefined
    }

    public eTrafficType type;
    public String trafficAirportDefinedTitle;
    public String trafficXmlDefinedTitle;
    public CustomTraffic customTraffic = new CustomTraffic();
    public int maxPlanes;
    public double densityPercentage;
    public boolean allowDelays;
    public double emergencyPerDayProbability = 1 / 7d;
  }

  public static class CustomTraffic {

    public boolean useExtendedCallsigns;
    public int weightTypeD;
    public int weightTypeC;
    public int weightTypeB;
    public int weightTypeA;
    public int arrivals2departuresRatio;
    public int[] movementsPerHour;
    public double nonCommercialFlightProbability;
    public String countryCodes = "OK";
    public String companies = "CSA;EZY";

    public String[] getCountryCodes() {
      return toModel(this.countryCodes);
    }

    public void setCountryCodes(String[] countryCodes) {
      this.countryCodes = fromModel(countryCodes);
    }

    public String[] getCompanies() {
      return toModel(companies);
    }

    public void setCompanies(String[] companies) {
      this.companies = fromModel(companies);
    }

    private String[] toModel(String value){
      String[] ret = value.split(";");
      IList<String> lst = new EList<>(ret);
      lst = lst.where(q->q != null).select(q->q.trim()).where(q->q.length() > 0);
      ret = lst.toArray(String.class);
      return ret;
    }

    private String fromModel(String[] values){
      IList<String> lst = new EList<>(values);
      EStringBuilder sb = new EStringBuilder();
      sb.appendItems(lst, q->q, ";");
      return sb.toString();
    }
  }

  public static class Radar {
    public String packClass = null;
  }

  public static class Simulation {
    public int secondLengthInMs = 1000;
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
    this.radar = new Radar();
  }
}
