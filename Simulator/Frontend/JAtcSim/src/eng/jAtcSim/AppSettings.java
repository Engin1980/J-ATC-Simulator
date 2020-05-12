package eng.jAtcSim;


import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;

import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.abstractRadar.settngs.RadarDisplaySettings;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppSettings {

  public static class Stats {
    public int snapshotIntervalDistance = 5;
  }

  public static class Radar {
    public static class DisplaySettings {
      public boolean tma = true;
      public boolean country = true;
      public boolean mrva = true;
      public boolean mrvaLabel = true;
      public boolean ctr = true;
      public boolean vor = true;
      public boolean ndb = true;
      public boolean airport = true;
      public boolean sid = true;
      public boolean star = true;
      public boolean fix = true;
      public boolean routeFix = true;
      public boolean minorFix = true;
      public boolean rings = true;
      public boolean history = true;
      public int minAltitude = 0;
      public int maxAltitude = 99000;

      public RadarDisplaySettings toRadarDisplaySettings() {
        RadarDisplaySettings ret = new RadarDisplaySettings();
        ret.setAirportVisible(this.airport);
        ret.setCountryBorderVisible(this.country);
        ret.setCtrBorderVisible(this.ctr);
        ret.setFixMinorVisible(this.minorFix);
        ret.setFixRouteVisible(this.routeFix);
        ret.setFixVisible(this.fix);
        ret.setMrvaBorderAltitudeVisible(this.mrvaLabel);
        ret.setMrvaBorderVisible(this.mrva);
        ret.setNdbVisible(this.ndb);
        ret.setRingsVisible(this.rings);
        ret.setSidVisible(this.sid);
        ret.setStarVisible(this.star);
        ret.setTmaBorderVisible(this.tma);
        ret.setVorVisible(this.vor);
        ret.setPlaneHistoryVisible(this.history);
        ret.setMinAltitude(this.minAltitude);
        ret.setMaxAltitude(this.maxAltitude);
        return ret;
      }
    }
    public Path styleSettingsFile;
    public int displayTextDelay;
    public DisplaySettings displaySettings;
  }

  public static class AutoSave {
    public int intervalInSeconds;
    public Path path;
  }
  @XmlIgnore
  private static Path applicationFolder;
  @XmlIgnore
  private static java.time.LocalDateTime startDateTime;
  @XmlIgnore
  private static boolean initialized = false;
  public AutoSave autosave = new AutoSave();
  public Radar radar = new Radar();
  public Path startupSettingsFile;
  public Path soundFolder;
  public Path logFolder;
  public Path stripSettingsFile;
  public Path speechFormatterFile;
  public Stats stats = new Stats();
  @XmlIgnore
  private FlightStripSettings flightStripSettings = null;

  public static Path getApplicationFolder() {
    return applicationFolder;
  }

  public static void init() {
    applicationFolder = Paths.get(System.getProperty("user.dir"));
    startDateTime = LocalDateTime.now();
    initialized = true;
  }

  public static Path tryGetAppSettingsFile() {
    Path ret;

    ret = getUnderAppFolder("appSettings.at.xml");
    if (ret.toFile().exists() == false) {
      ret = getUnderAppFolder(Paths.get("_SettingFiles", "appSettings.at.xml").toString());
      if (ret.toFile().exists() == false)
        ret = null;
    }

    return ret;
  }

  public static AppSettings create() {
    AppSettings ret = new AppSettings();

    Path appPath = tryGetAppSettingsFile();
    if (appPath != null) {
      XDocument doc;
      try {
        doc = XDocument.load(appPath);
      } catch (EXmlException ex) {
        throw new EApplicationException("App settings file was not loaded.", ex);
      }

      String tmp;

      tmp = doc.getRoot().getChild("startupSettingsFile").getContent();
      ret.startupSettingsFile = decodePath(tmp);

      tmp = doc.getRoot().getChild("soundFolder").getContent();
      ret.soundFolder = decodePath(tmp);

      tmp = doc.getRoot().getChild("logFolder").getContent();
      ret.logFolder = decodePath(tmp);

      tmp = doc.getRoot().getChild("stripSettingsFile").getContent();
      ret.stripSettingsFile = decodePath(tmp);

      tmp = doc.getRoot().getChild("speechFormatterFile").getContent();
      ret.speechFormatterFile = decodePath(tmp);

      XElement radarElement = doc.getRoot().getChild("radar");
      tmp = radarElement.getChild("styleSettingsFile").getContent();
      ret.radar.styleSettingsFile = decodePath(tmp);
      ret.radar.displayTextDelay = Integer.parseInt(radarElement.getChild("displayTextDelay").getContent());
      ret.radar.displaySettings = new XmlSerializer().deserialize(
          radarElement.getChild("displaySettings"), Radar.DisplaySettings.class);

      XElement autosaveElement = doc.getRoot().getChild("autosave");
      ret.autosave.intervalInSeconds = Integer.parseInt(autosaveElement.getAttribute("intervalInMinutes")) * 60;
      tmp = autosaveElement.getAttribute("path");
      ret.autosave.path = decodePath(tmp);

      XElement statsElement = doc.getRoot().getChild("stats");
      ret.stats.snapshotIntervalDistance = Integer.parseInt(statsElement.getAttribute("snapshotIntervalDistance"));
    }

    return ret;
  }

  private static Path decodePath(String tmp) {
    if (tmp.contains("$time$")) {
      String timeString = startDateTime.format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss"));
      tmp = tmp.replaceAll("\\$time\\$", timeString);
    }
    Path ret;
    if (tmp.startsWith("$app$"))
      ret = Paths.get(applicationFolder.toAbsolutePath().toString(), tmp.substring(5));
    else if (tmp.startsWith("$user$"))
      ret = Paths.get(System.getProperty("user.dir"), tmp.substring(5));
    else
      ret = Paths.get(tmp);

    return ret;
  }

  private static Path getUnderAppFolder(String path) {
    Path ret = Paths.get(applicationFolder.toString(), path);
    return ret;
  }

  public FlightStripSettings getLoadedFlightStripSettings(){
    if (flightStripSettings == null){
      try {
        this.flightStripSettings =
            XmlLoadHelper.loadStripSettings(this.stripSettingsFile.toString());
      }catch (Exception ex){
        throw new EApplicationException("Failed to load flight-strip-settings from " + this.startupSettingsFile.toString(), ex);
      }
    }
    return this.flightStripSettings;
  }

  private AppSettings() {
    if (!initialized)
      throw new EApplicationException("Unable to create a new instance of AppSettings. Static method 'AppSettings.init()' must be called first.");

    this.startupSettingsFile = getUnderAppFolder("defaultStartupSettings.ss.xml");
    this.soundFolder = getUnderAppFolder("_Sounds");
    this.logFolder = getUnderAppFolder("_Log");
    this.stripSettingsFile = getUnderAppFolder("stripSettings.at.xml");
    this.radar.styleSettingsFile = getUnderAppFolder("radarStyleSettings.at.xml");
  }
}