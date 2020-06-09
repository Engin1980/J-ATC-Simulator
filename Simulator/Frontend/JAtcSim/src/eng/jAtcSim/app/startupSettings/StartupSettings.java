package eng.jAtcSim.app.startupSettings;

import eng.eSystem.EStringBuilder;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.eXml.XElement;
import eng.eXmlSerialization.XmlSerializer;

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
    public String companiesFleetsXmlFile;
    public String generalAviationFleetsXmlFile;
    public String planesXmlFile;
    public String trafficXmlFile;
    public String weatherXmlFile;

    public void normalizeSlashes() {
      this.trafficXmlFile = normalizePath(this.trafficXmlFile);
      this.planesXmlFile = normalizePath(this.planesXmlFile);
      this.areaXmlFile = normalizePath(this.areaXmlFile);
      this.generalAviationFleetsXmlFile = normalizePath(this.generalAviationFleetsXmlFile);
      this.companiesFleetsXmlFile = normalizePath(this.companiesFleetsXmlFile);
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
    XmlSerializer ser = new XmlSerializer();
    StartupSettings ret = ser.deserialize(source, StartupSettings.class);
    return ret;

//    XElement elm;
//    elm = source.getChild("files");
//    XmlLoaderUtils.loadPrimitiveAttribute(elm, ret.files, "areaXmlFile",
//        "generalAviationFleetsXmlFile", "companiesFleetsXmlFile", "planesXmlFile",
//        "trafficXmlFile", "weatherXmlFile");
//
//    elm = source.getChild("recent");
//    XmlLoaderUtils.loadPrimitiveAttribute(elm, ret.recent, "icao");
//    elm.setAttribute("time", ret.recent.time.format(
//        DateTimeFormatter.ofPattern("HH:mm")));
//
//    elm = source.getChild("weather");
//    XmlLoaderUtils.loadPrimitiveAttribute(elm, ret.weather, "cloudBaseAltitudeFt", "cloudBaseProbability",
//        "snowState", "type",
//        "visibilityInM", "windDirection", "windSpeed");
//
//    elm = new XElement("simulation");
//    XmlLoaderUtils.loadPrimitiveAttribute(elm, ret.simulation, "secondLengthInMs");
//
//    elm = new XElement("radar");
//    XmlSaverUtils.savePrimitiveAttribute(elm, ret.radar, "packClass");
//
//    elm = new XElement("traffic");
//    XmlSaverUtils.savePrimitiveAttribute(elm, ret.traffic, "allowDelays", "densityPercentage",
//        "emergencyPerDayProbability", "maxPlanes", "type");
//    {
//      elm = elm.getChild("customTraffic");
//      XmlSaverUtils.savePrimitiveAttribute(elm, ret.traffic.customTraffic, "arrivals2departuresRatio", "companies",
//          "countryCodes", "nonCommercialFlightProbability", "useExtendedCallsigns",
//          "weightTypeA", "weightTypeB", "weightTypeC", "weightTypeD");
//      String data = elm.getAttribute("movementsPerHour");
//      String[] pts = data.split(";");
//      IList<String> lstPts = new EList<>(pts);
//      IList<Integer> intPts = lstPts.select(q -> Integer.parseInt(q));
//      ret.traffic.customTraffic.movementsPerHour = ArrayUtils.toPrimitive(intPts.toArray(int.class));
//    }
//
//    return ret;
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

    XmlSerializer ser = new XmlSerializer();
    ser.serialize(this, target);

//    XElement elm;
//    elm = new XElement("files");
//    XmlSaverUtils.savePrimitiveAttribute(elm, files, "areaXmlFile",
//        "companiesFleetsXmlFile", "generalAviationFleetsXmlFile",
//        "planesXmlFile",
//        "trafficXmlFile", "weatherXmlFile");
//    target.addElement(elm);
//
//    elm = new XElement("recent");
//    XmlSaverUtils.savePrimitiveAttribute(elm, recent, "icao");
//    elm.setAttribute("time", recent.time.format(
//        DateTimeFormatter.ofPattern("HH:mm")));
//    target.addElement(elm);
//
//    elm = new XElement("weather");
//    XmlSaverUtils.savePrimitiveAttribute(elm, files, "cloudBaseAltitudeFt", "cloudBaseProbability",
//        "snowState", "type",
//        "visibilityInM", "windDirection", "windSpeed");
//    target.addElement(elm);
//
//    elm = new XElement("simulation");
//    XmlSaverUtils.savePrimitiveAttribute(elm, simulation, "secondLengthInMs");
//    target.addElement(elm);
//
//    elm = new XElement("radar");
//    XmlSaverUtils.savePrimitiveAttribute(elm, radar, "packClass");
//    target.addElement(elm);
//
//    elm = new XElement("traffic");
//    XmlSaverUtils.savePrimitiveAttribute(elm, traffic, "allowDelays", "densityPercentage",
//        "emergencyPerDayProbability", "maxPlanes", "type");
//    {
//      XElement tmp = new XElement("customTraffic");
//      elm.addElement(tmp);
//      XmlSaverUtils.savePrimitiveAttribute(elm, traffic, "arrivals2departuresRatio", "companies",
//          "countryCodes", "nonCommercialFlightProbability", "useExtendedCallsigns",
//          "weightTypeA", "weightTypeB", "weightTypeC", "weightTypeD");
//      EStringBuilder sb = new EStringBuilder();
//      sb.appendItems(ArrayUtils.toWrapper(this.traffic.customTraffic.movementsPerHour), ";");
//      tmp.setAttribute("movementsPerHour", sb.toString());
//    }
//    target.addElement(elm);
  }
}
