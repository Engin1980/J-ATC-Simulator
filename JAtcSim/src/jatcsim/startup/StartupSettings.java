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

  private static final int AREA_XML_FILE = 0;
  private static final int PLANES_XML_FILES = 1;
  private static final int ICAO = 2;
  
  private static final int TIME = 6;
  
  private static final int WEATHER_USER_METAR = 3;
  private static final int WEATHER_ONLINE = 4;
  private static final int WEATHER_USER_CHANGES = 5;

  private static final Map<Integer, Way> maps;

  static {
    maps = new HashMap();
    maps.put(AREA_XML_FILE, new Way("Xml", "areaXmlFile"));
    maps.put(PLANES_XML_FILES, new Way("Xml", "planesXmlFile"));
    
    maps.put(ICAO, new Way("Recent", "recentICAO"));
    maps.put(TIME, new Way("Recent", "time"));
    
    maps.put(WEATHER_ONLINE, new Way("Weather", "useOnline"));
    maps.put(WEATHER_USER_METAR, new Way("Weather", "metar"));
    maps.put(WEATHER_USER_CHANGES, new Way("Weather", "userChanges"));
  }

  private String areaXmlFile;
  private String planesXmlFile;
  
  private String recentIcao;
  private String recentTime;
  
  private String weatherUserMetar;
  private boolean weatherOnline;
  private int weatherUserChanges;

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

  public static StartupSettings tryLoad() {
    StartupSettings ret = new StartupSettings();
    String iniFileName = jatcsim.JAtcSim.resFolder.toString() + "\\settings\\config.ini";
    String tmp;

    IniFile inf = IniFile.tryLoad(iniFileName);

    ret.areaXmlFile = getValue(inf, maps.get(AREA_XML_FILE));
    ret.planesXmlFile = getValue(inf, maps.get(PLANES_XML_FILES));
    
    ret.recentIcao =getValue(inf, maps.get(ICAO));
    ret.recentTime = getValue(inf, maps.get(TIME));
    
    tmp = getValue(inf, maps.get(WEATHER_ONLINE));
    ret.weatherOnline = tmp != null && tmp.equals("1");
    tmp = getValue(inf, maps.get(WEATHER_USER_CHANGES));
    if (tmp != null)
      ret.weatherUserChanges = Integer.parseInt(tmp);
    else
      ret.weatherUserChanges = 0;
    ret.weatherUserMetar = getValue(inf, maps.get(WEATHER_USER_METAR));

    return ret;
  }

  public void save() {
    StartupSettings ret = new StartupSettings();
    String iniFileName = jatcsim.JAtcSim.resFolder.toString() + "\\settings\\config.ini";

    IniFile inf = new IniFile();

    setValue(inf, maps.get(AREA_XML_FILE), this.areaXmlFile);
    setValue(inf, maps.get(PLANES_XML_FILES), this.planesXmlFile);
    
    setValue(inf, maps.get(ICAO), this.recentIcao);
    setValue(inf, maps.get(TIME), this.recentTime);
    
    setValue(inf, maps.get(WEATHER_ONLINE), this.weatherOnline ? "1" : "0");
    setValue(inf, maps.get(WEATHER_USER_CHANGES), Integer.toString(this.weatherUserChanges));
    setValue(inf, maps.get(WEATHER_USER_METAR),  this.weatherUserMetar);

    try {
      inf.save(iniFileName);
    } catch (IOException ex) {
      //TODO log that saving failed
      System.out.println("Failed to save config.ini.");
    }
  }

  public String getRecentTime() {
    return recentTime;
  }

  public void setRecentTime(String recentTime) {
    this.recentTime = recentTime;
  }

  private static String getValue(IniFile inf, Way way) {
    String ret = inf.getValue(way.section, way.key);
    return ret;
  }

  private static void setValue(IniFile inf, Way way, String value) {
    inf.setValue(way.section, way.key, value);
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
  
}

class Way {

  public String section;
  public String key;

  public Way(String section, String key) {
    this.section = section;
    this.key = key;
  }
}
