/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

import eng.eIni.IniFile;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marek Vajgl
 */
public class StartupSettings {

  private static int IDCNT = 1;
  
  private static final int AREA_XML_FILE = IDCNT++;
  private static final int PLANES_XML_FILES = IDCNT++;
  private static final int ICAO = IDCNT++;

  private static final int TIME = IDCNT++;

  private static final int WEATHER_USER_METAR = IDCNT++;
  private static final int WEATHER_ONLINE = IDCNT++;
  private static final int WEATHER_USER_CHANGES = IDCNT++;

  private static final int TRAFFIC_USE_XML = IDCNT++;
  private static final int TRAFFIC_XML_FILE = IDCNT++;
  private static final int TRAFFIC_CUSTOM_MOVEMENTS = IDCNT++;
  private static final int TRAFFIC_CUSTOM_ARRIVALS2DEPARTURES = IDCNT++;
  private static final int TRAFFIC_CUSTOM_VFR2IFR = IDCNT++;
  private static final int TRAFFIC_CUSTOM_A_TYPE_WEIGHT = IDCNT++;
  private static final int TRAFFIC_CUSTOM_B_TYPE_WEIGHT = IDCNT++;
  private static final int TRAFFIC_CUSTOM_C_TYPE_WEIGHT = IDCNT++;
  private static final int TRAFFIC_CUSTOM_D_TYPE_WEIGHT = IDCNT++;
  private static final int TRAFFIC_XML_DELAY_ALLOWED = IDCNT++;
  private static final int TRAFFIC_CUSTOM_USING_EXTENDED_CALLSIGNS = IDCNT++;
  private static final int TRAFFIC_CUSTOM_MAX_PLANES = IDCNT++;
  
  private static final int RADAR_PACK_CLASS = IDCNT++;
  
  private static final int SIMULATION_SPEED = IDCNT++;

  private static final Map<Integer, Way> maps;

  static {
    maps = new HashMap();
    maps.put(AREA_XML_FILE, new Way("Xml", "areaXmlFile"));
    maps.put(PLANES_XML_FILES, new Way("Xml", "planesXmlFile"));
    maps.put(TRAFFIC_XML_FILE, new Way("Xml", "trafficXmlFile"));

    maps.put(ICAO, new Way("Recent", "recentICAO"));
    maps.put(TIME, new Way("Recent", "time"));

    maps.put(WEATHER_ONLINE, new Way("Weather", "useOnline"));
    maps.put(WEATHER_USER_METAR, new Way("Weather", "metar"));
    maps.put(WEATHER_USER_CHANGES, new Way("Weather", "userChanges"));

    maps.put(TRAFFIC_USE_XML, new Way("Traffic", "useXml"));
    maps.put(TRAFFIC_CUSTOM_MOVEMENTS, new Way("Traffic", "movementsPerHour"));
    maps.put(TRAFFIC_CUSTOM_ARRIVALS2DEPARTURES, new Way("Traffic", "arrivals2departuresRatio"));
    maps.put(TRAFFIC_CUSTOM_VFR2IFR, new Way("Traffic", "vfr2ifrRatio"));
    maps.put(TRAFFIC_CUSTOM_A_TYPE_WEIGHT, new Way("Traffic", "weightTypeA"));
    maps.put(TRAFFIC_CUSTOM_B_TYPE_WEIGHT, new Way("Traffic", "weightTypeB"));
    maps.put(TRAFFIC_CUSTOM_C_TYPE_WEIGHT, new Way("Traffic", "weightTypeC"));
    maps.put(TRAFFIC_CUSTOM_D_TYPE_WEIGHT, new Way("Traffic", "weightTypeD"));
    maps.put(TRAFFIC_XML_DELAY_ALLOWED, new Way("Traffic", "delayAllowed"));
    maps.put(TRAFFIC_CUSTOM_USING_EXTENDED_CALLSIGNS, new Way("Traffic", "useExtendedCallsigns"));
    maps.put(TRAFFIC_CUSTOM_MAX_PLANES, new Way("Traffic", "maxPlanes"));
    
    maps.put(RADAR_PACK_CLASS, new Way("Radar", "radar"));
    
    maps.put(SIMULATION_SPEED, new Way("Simulation", "secondLengthInMs"));
  }

  private String areaXmlFile;
  private String planesXmlFile;

  private String recentIcao;
  private String recentTime;

  private String weatherUserMetar;
  private boolean weatherOnline;
  private int weatherUserChanges;

  private boolean trafficUseXml;
  private String trafficXmlFile;
  private int trafficCustomMovements;
  private int trafficCustomArrivals2Departures;
  private int trafficCustomVfr2Ifr;
  private int trafficCustomWeightTypeA;
  private int trafficCustomWeightTypeB;
  private int trafficCustomWeightTypeC;
  private int trafficCustomWeightTypeD;
  private boolean trafficXmlDelayAllowed;
  private boolean trafficCustomUsingExtendedCallsigns;
  private int trafficCustomMaxPlanes;
  
  private String radarPackClassName;
  
  private int simulationSecondLengthInMs;
  
  
  public static StartupSettings tryLoad() {
    StartupSettings ret = new StartupSettings();
    String iniFileName = jatcsim.JAtcSim.resFolder.toString() + "\\settings\\config.ini";

    IniFile inf = IniFile.tryLoad(iniFileName);

    ret.areaXmlFile = getString(inf, AREA_XML_FILE);
    ret.planesXmlFile = getString(inf, PLANES_XML_FILES);

    ret.recentIcao = getString(inf, ICAO);
    ret.recentTime = getString(inf,TIME);

    ret.weatherOnline = getBoolean(inf, WEATHER_ONLINE, true);
    ret.weatherUserChanges = getInt(inf, WEATHER_USER_CHANGES, 0);
    ret.weatherUserMetar = getString(inf, WEATHER_USER_METAR);

    ret.trafficUseXml = getBoolean(inf, TRAFFIC_USE_XML, false);
    ret.trafficXmlFile = getString(inf, TRAFFIC_XML_FILE);
    ret.trafficCustomMovements = getInt(inf, TRAFFIC_CUSTOM_MOVEMENTS, 10);
    ret.trafficCustomArrivals2Departures = getInt(inf, TRAFFIC_CUSTOM_ARRIVALS2DEPARTURES, 5);
    ret.trafficCustomVfr2Ifr = 10; // getInt(inf, TRAFFIC_CUSTOM_VFR2IFR, 8);
    ret.trafficCustomWeightTypeA = getInt(inf, TRAFFIC_CUSTOM_A_TYPE_WEIGHT, 5);
    ret.trafficCustomWeightTypeB = getInt(inf, TRAFFIC_CUSTOM_B_TYPE_WEIGHT, 5);
    ret.trafficCustomWeightTypeC = getInt(inf, TRAFFIC_CUSTOM_C_TYPE_WEIGHT, 5);
    ret.trafficCustomWeightTypeD = getInt(inf, TRAFFIC_CUSTOM_D_TYPE_WEIGHT, 5);
    ret.trafficXmlDelayAllowed = getBoolean(inf, TRAFFIC_XML_DELAY_ALLOWED, true);
    ret.trafficCustomUsingExtendedCallsigns = getBoolean(inf, TRAFFIC_CUSTOM_USING_EXTENDED_CALLSIGNS, false);
    ret.trafficCustomMaxPlanes = getInt(inf, TRAFFIC_CUSTOM_MAX_PLANES, 15);

    ret.radarPackClassName = getString(inf, RADAR_PACK_CLASS, "jatcsim.frmPacks.simple.Pack");
    
    ret.simulationSecondLengthInMs = getInt(inf, SIMULATION_SPEED, 750);
    
    return ret;
  }

  public void save() {
    String iniFileName = jatcsim.JAtcSim.resFolder.toString() + "\\settings\\config.ini";

    IniFile inf = new IniFile();

    setString(inf,  AREA_XML_FILE, this.areaXmlFile);
    setString(inf,  PLANES_XML_FILES, this.planesXmlFile);

    setString(inf,  ICAO, this.recentIcao);
    setString(inf,  TIME, this.recentTime);

    setBoolean(inf,  WEATHER_ONLINE, this.weatherOnline);
    setInt(inf,  WEATHER_USER_CHANGES, this.weatherUserChanges);
    setString(inf,  WEATHER_USER_METAR, this.weatherUserMetar);

    setBoolean(inf,  TRAFFIC_USE_XML, this.trafficUseXml);
    setString(inf,  TRAFFIC_XML_FILE, this.trafficXmlFile);
    setInt(inf,  TRAFFIC_CUSTOM_MOVEMENTS, this.trafficCustomMovements);
    setInt(inf,  TRAFFIC_CUSTOM_ARRIVALS2DEPARTURES, this.trafficCustomArrivals2Departures);
    setInt(inf,  trafficCustomVfr2Ifr, this.trafficCustomVfr2Ifr);
    setInt(inf,  TRAFFIC_CUSTOM_A_TYPE_WEIGHT, this.trafficCustomWeightTypeA);
    setInt(inf,  TRAFFIC_CUSTOM_B_TYPE_WEIGHT, this.trafficCustomWeightTypeB);
    setInt(inf,  TRAFFIC_CUSTOM_C_TYPE_WEIGHT, this.trafficCustomWeightTypeC);
    setInt(inf,  TRAFFIC_CUSTOM_D_TYPE_WEIGHT, this.trafficCustomWeightTypeD);
    setBoolean(inf, TRAFFIC_XML_DELAY_ALLOWED, this.trafficXmlDelayAllowed);
    setBoolean(inf, TRAFFIC_CUSTOM_USING_EXTENDED_CALLSIGNS, this.trafficCustomUsingExtendedCallsigns);
    setInt(inf, TRAFFIC_CUSTOM_MAX_PLANES, this.trafficCustomMaxPlanes);

    setString(inf, RADAR_PACK_CLASS, this.radarPackClassName);
    
    setInt(inf, SIMULATION_SPEED, this.simulationSecondLengthInMs);
    
    try {
      inf.save(iniFileName);
    } catch (IOException ex) {
      //TODO log that saving failed
      System.out.println("Failed to save config.ini.");
    }
  }

  private static String getString(IniFile inf, int wayKey, String defaultValue){
    String ret = getString(inf, wayKey);
    if (ret == null || ret.isEmpty())
      ret = defaultValue;
    return ret;
  }
  
  private static String getString(IniFile inf, int wayKey) {
    Way way = maps.get(wayKey);
    String ret = inf.getValue(way.section, way.key);
    return ret;
  }

  private static void setString(IniFile inf, int wayKey, String value) {
    Way way = maps.get(wayKey);
    inf.setValue(way.section, way.key, value);
  }

  private static void setInt(IniFile inf, int wayKey, int value) {
    String s = Integer.toString(value);
    setString(inf, wayKey, s);
  }

  private static void setBoolean(IniFile inf, int wayKey, boolean value) {
    String s = Boolean.toString(value);
    setString(inf, wayKey, s);
  }

  private static int getInt(IniFile inf, int wayKey, int defaultValue) {
    String s = getString(inf, wayKey);
    int ret;
    try {
      ret = Integer.parseInt(s);
    } catch (Exception ex) {
      ret = defaultValue;
    }
    return ret;
  }

  private static boolean getBoolean(IniFile inf, int wayKey, boolean defaultValue) {
    String s = getString(inf, wayKey);
    boolean ret;
    try {
      ret = Boolean.parseBoolean(s);
    } catch (Exception ex) {
      ret = defaultValue;
    }
    return ret;
  }
  
  
  
  
  
  
  
  
  
  
  
  
  public boolean isTrafficXmlDelayAllowed() {
    return trafficXmlDelayAllowed;
  }

  public void setTrafficXmlDelayAllowed(boolean trafficXmlDelayAllowed) {
    this.trafficXmlDelayAllowed = trafficXmlDelayAllowed;
  }

  public String getRecentIcao() {
    return recentIcao;
  }

  public String getWeatherUserMetar() {
    return weatherUserMetar;
  }

  public void setWeatherUserMetar(String weatherUserMetar) {
    this.weatherUserMetar = weatherUserMetar;
  }

  public boolean isWeatherOnline() {
    return weatherOnline;
  }

  public void setWeatherOnline(boolean weatherOnline) {
    this.weatherOnline = weatherOnline;
  }

  public int getWeatherUserChanges() {
    return weatherUserChanges;
  }

  public void setWeatherUserChanges(int weatherUserChanges) {
    this.weatherUserChanges = weatherUserChanges;
  }

  public void setRecentIcao(String recentIcao) {
    this.recentIcao = recentIcao;
  }

  public String getRecentTime() {
    return recentTime;
  }

  public void setRecentTime(String recentTime) {
    this.recentTime = recentTime;
  }


  public String getAreaXmlFile() {
    return areaXmlFile;
  }

  public void setAreaXmlFile(String areaXmlFile) {
    this.areaXmlFile = areaXmlFile;
  }

  public String getPlanesXmlFile() {
    return planesXmlFile;
  }

  public void setPlanesXmlFile(String planesXmlFile) {
    this.planesXmlFile = planesXmlFile;
  }

  public boolean isTrafficUseXml() {
    return trafficUseXml;
  }

  public void setTrafficUseXml(boolean trafficUseXml) {
    this.trafficUseXml = trafficUseXml;
  }

  public String getTrafficXmlFile() {
    return trafficXmlFile;
  }

  public void setTrafficXmlFile(String trafficXmlFile) {
    this.trafficXmlFile = trafficXmlFile;
  }

  public int getTrafficCustomMovements() {
    return trafficCustomMovements;
  }

  public void setTrafficCustomMovements(int trafficCustomMovements) {
    this.trafficCustomMovements = trafficCustomMovements;
  }

  public int getTrafficCustomArrivals2Departures() {
    return trafficCustomArrivals2Departures;
  }

  public void setTrafficCustomArrivals2Departures(int trafficCustomArrivals2Departures) {
    this.trafficCustomArrivals2Departures = trafficCustomArrivals2Departures;
  }

  public int getTrafficCustomVfr2Ifr() {
    return trafficCustomVfr2Ifr;
  }

  public void setTrafficCustomVfr2Ifr(int trafficCustomVfr2Ifr) {
    this.trafficCustomVfr2Ifr = trafficCustomVfr2Ifr;
  }

  public int getTrafficCustomWeightTypeA() {
    return trafficCustomWeightTypeA;
  }

  public void setTrafficCustomWeightTypeA(int trafficCustomWeightTypeA) {
    this.trafficCustomWeightTypeA = trafficCustomWeightTypeA;
  }

  public int getTrafficCustomWeightTypeB() {
    return trafficCustomWeightTypeB;
  }

  public void setTrafficCustomWeightTypeB(int trafficCustomWeightTypeB) {
    this.trafficCustomWeightTypeB = trafficCustomWeightTypeB;
  }

  public int getTrafficCustomWeightTypeC() {
    return trafficCustomWeightTypeC;
  }

  public void setTrafficCustomWeightTypeC(int trafficCustomWeightTypeC) {
    this.trafficCustomWeightTypeC = trafficCustomWeightTypeC;
  }

  public int getTrafficCustomWeightTypeD() {
    return trafficCustomWeightTypeD;
  }

  public void setTrafficCustomWeightTypeD(int trafficCustomWeightTypeD) {
    this.trafficCustomWeightTypeD = trafficCustomWeightTypeD;
  }

  public String getRadarPackClassName() {
    return radarPackClassName;
  }

  public void setRadarPackClassName(String radarPackClassName) {
    this.radarPackClassName = radarPackClassName;
  }

  public boolean isTrafficCustomUsingExtendedCallsigns() {
    return trafficCustomUsingExtendedCallsigns;
  }

  public void setTrafficCustomUsingExtendedCallsigns(boolean trafficCustomUsingExtendedCallsigns) {
    this.trafficCustomUsingExtendedCallsigns = trafficCustomUsingExtendedCallsigns;
  }

  public static int getIDCNT() {
    return IDCNT;
  }

  public static void setIDCNT(int IDCNT) {
    StartupSettings.IDCNT = IDCNT;
  }

  public int getTrafficCustomMaxPlanes() {
    return trafficCustomMaxPlanes;
  }

  public void setTrafficCustomMaxPlanes(int trafficCustomMaxPlanes) {
    this.trafficCustomMaxPlanes = trafficCustomMaxPlanes;
  }

  public int getSimulationSecondLengthInMs() {
    return simulationSecondLengthInMs;
  }

  public void setSimulationSecondLengthInMs(int simulationSecondLengthInMs) {
    this.simulationSecondLengthInMs = simulationSecondLengthInMs;
  }
  
}

class Way {

  public String section;
  public String key;

  public Way(String section, String key) {
    this.section = section;
    this.key = key;
  }
}
