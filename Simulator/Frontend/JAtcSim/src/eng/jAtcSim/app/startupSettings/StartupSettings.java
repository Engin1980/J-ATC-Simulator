package eng.jAtcSim.app.startupSettings;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.utilites.ArrayUtils;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.shared.xml.XmlSaverUtils;

import java.time.LocalTime;

public class StartupSettings {

  public static class Files {
    private static String normalizePath(String path) {
      String ret;
      if (path == null)
        ret = null;
      else
        ret = path.replace('\\', '/');
      return ret;
    }

    public String areaXmlFile;
    public String fleetsXmlFile;
    public String planesXmlFile;
    public String trafficXmlFile;
    public String weatherXmlFile;

    public void normalizeSlashes() {
      this.trafficXmlFile = normalizePath(this.trafficXmlFile);
      this.planesXmlFile = normalizePath(this.planesXmlFile);
      this.areaXmlFile = normalizePath(this.areaXmlFile);
      this.fleetsXmlFile = normalizePath(this.fleetsXmlFile);
      this.weatherXmlFile = normalizePath(this.weatherXmlFile);
    }
  }

  public static class Recent {
    public String icao;
    public LocalTime time;
  }

  public static class Weather {
    public enum WeatherSourceType {
      user,
      online,
      xml
    }

    public enum eSnowState {
      none,
      normal,
      intensive
    }

    public int cloudBaseAltitudeFt;
    public double cloudBaseProbability;
    public eSnowState snowState;
    public WeatherSourceType type = WeatherSourceType.user;
    public int visibilityInM;
    public int windDirection;
    public int windSpeed;
  }

  public static class Traffic {
    public enum eTrafficType {
      user,
      xml
    }

    public boolean allowDelays;
    public CustomTraffic customTraffic = new CustomTraffic();
    public double densityPercentage;
    public double emergencyPerDayProbability = 1 / 7d;
    public int maxPlanes;
    public eTrafficType type;
  }

  public static class CustomTraffic {

    public static void load(XElement source, CustomTraffic target) {
      XmlLoaderUtils.loadObject(source, target, EMap.of(
          "movementsPerHour", e -> {
            String data = e.getContent();
            String[] pts = data.split(";");
            IList<String> lstPts = new EList<>(pts);
            IList<Integer> intPts = lstPts.select(q -> Integer.parseInt(q));
            target.movementsPerHour = ArrayUtils.toPrimitive(intPts.toArray(int.class));
          }));
    }

    public int arrivals2departuresRatio;
    public String companies = "CSA;EZY";
    public String countryCodes = "OK";
    public int[] movementsPerHour;
    public double nonCommercialFlightProbability;
    public boolean useExtendedCallsigns;
    public int weightTypeA;
    public int weightTypeB;
    public int weightTypeC;
    public int weightTypeD;

    public String[] getCompanies() {
      return toModel(companies);
    }

    public XElement save(XElement elm) {
      XElement ret = XmlSaverUtils.saveObject(this, xmlElementName,EMap.of(
         "movementsPerHour", (trgElm) -> {
           EStringBuilder sb = new EStringBuilder();
           sb.appendItems(ArrayUtils.toWrapper(this.movementsPerHour), ";");
           trgElm.setContent(sb.toString());
          }
      ));
      return ret;
    }

    public void setCompanies(String[] companies) {
      this.companies = fromModel(companies);
    }

    public String[] getCountryCodes() {
      return toModel(this.countryCodes);
    }

    public void setCountryCodes(String[] countryCodes) {
      this.countryCodes = fromModel(countryCodes);
    }

    private String fromModel(String[] values) {
      IList<String> lst = new EList<>(values);
      EStringBuilder sb = new EStringBuilder();
      sb.appendItems(lst, q -> q, ";");
      return sb.toString();
    }

    private String[] toModel(String value) {
      String[] ret = value.split(";");
      IList<String> lst = new EList<>(ret);
      lst = lst.where(q -> q != null).select(q -> q.trim()).where(q -> q.length() > 0);
      ret = lst.toArray(String.class);
      return ret;
    }
  }

  public static class Radar {
    public String packClass = null;
  }

  public static class Simulation {
    public int secondLengthInMs = 1000;
  }

  public static StartupSettings load(XElement source) {
    StartupSettings ret = new StartupSettings();
    XmlLoaderUtils.loadObject(source.getChild("files"), ret.files);
    XmlLoaderUtils.loadObject(source.getChild("recent"), ret.recent);
    XmlLoaderUtils.loadObject(source.getChild("weather"), ret.weather);
    XmlLoaderUtils.loadObject(source.getChild("simulation"), ret.simulation);
    XmlLoaderUtils.loadObject(source.getChild("radar"), ret.radar);
    XmlLoaderUtils.loadObject(source.getChild("traffic"), ret.traffic, EMap.of(
        "customTraffic", e -> CustomTraffic.load(e.getChild("customTraffic"), ret.traffic.customTraffic))
    );
    return ret;
  }

  public final Files files;
  public final Radar radar;
  public final Recent recent;
  public final Simulation simulation;
  public final Traffic traffic;
  public final Weather weather;

  public StartupSettings() {
    this.files = new Files();
    this.recent = new Recent();
    this.weather = new Weather();
    this.traffic = new Traffic();
    this.simulation = new Simulation();
    this.radar = new Radar();
  }

  public void save(XElement target) {
    target.addElement(
      XmlSaverUtils.saveObject(this.files, "files"));
    target.addElement(
        XmlSaverUtils.saveObject(this.recent, "recent"));
    target.addElement(
        XmlSaverUtils.saveObject(this.weather, "weather"));
    target.addElement(
        XmlSaverUtils.saveObject(this.simulation, "simulation"));
    target.addElement(
        XmlSaverUtils.saveObject(this.radar, "radar"));
    target.addElement(
        XmlSaverUtils.saveObject(this.traffic, "traffic", EMap.of(
            "customTraffic", (trg) -> this.traffic.customTraffic.save("customTraffic"))
        ));
  }
}
