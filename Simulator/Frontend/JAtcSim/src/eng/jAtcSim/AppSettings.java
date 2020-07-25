package eng.jAtcSim;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.annotations.XmlIgnored;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.frmPacks.shared.FlightStripSettings;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.xmlLoading.XmlSerialization;
import eng.jAtcSim.xmlLoading.XmlSerializationFactory;

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
      public static DisplaySettings load(XElement elm) {
        DisplaySettings ret = new DisplaySettings();

        ret.tma = SmartXmlLoaderUtils.loadBoolean(elm, "tma");
        ret.country = SmartXmlLoaderUtils.loadBoolean(elm, "country");
        ret.mrva = SmartXmlLoaderUtils.loadBoolean(elm, "mrva");
        ret.mrvaLabel = SmartXmlLoaderUtils.loadBoolean(elm, "mrvaLabel");
        ret.ctr = SmartXmlLoaderUtils.loadBoolean(elm, "ctr");
        ret.vor = SmartXmlLoaderUtils.loadBoolean(elm, "vor");
        ret.ndb = SmartXmlLoaderUtils.loadBoolean(elm, "ndb");
        ret.sid = SmartXmlLoaderUtils.loadBoolean(elm, "sid");
        ret.star = SmartXmlLoaderUtils.loadBoolean(elm, "star");
        ret.fix = SmartXmlLoaderUtils.loadBoolean(elm, "fix");
        ret.routeFix = SmartXmlLoaderUtils.loadBoolean(elm, "routeFix");
        ret.minorFix = SmartXmlLoaderUtils.loadBoolean(elm, "minorFix");
        ret.rings = SmartXmlLoaderUtils.loadBoolean(elm, "rings");
        ret.history = SmartXmlLoaderUtils.loadBoolean(elm, "history");
        ret.minAltitude = SmartXmlLoaderUtils.loadInteger(elm, "minAltitude");
        ret.maxAltitude = SmartXmlLoaderUtils.loadInteger(elm, "maxAltitude");
        return ret;
      }
      public boolean airport = true;
      public boolean country = true;
      public boolean ctr = true;
      public boolean fix = true;
      public boolean history = true;
      public int maxAltitude = 99000;
      public int minAltitude = 0;
      public boolean minorFix = true;
      public boolean mrva = true;
      public boolean mrvaLabel = true;
      public boolean ndb = true;
      public boolean rings = true;
      public boolean routeFix = true;
      public boolean sid = true;
      public boolean star = true;
      public boolean tma = true;
      public boolean vor = true;

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
    public DisplaySettings displaySettings;
    public int displayTextDelay;
    public Path styleSettingsFile;
  }

  public static class AutoSave {
    public int intervalInSeconds;
    public Path path;
  }

  @XmlIgnored
  private static Path applicationFolder;
  @XmlIgnored
  private static boolean initialized = false;
  @XmlIgnored
  private static java.time.LocalDateTime startDateTime;

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
      ret.radar.displaySettings = Radar.DisplaySettings.load(radarElement.getChild("displaySettings"));
      //) new XmlSerializer().deserialize(
      //    radarElement.getChild("displaySettings"), Radar.DisplaySettings.class);

      XElement autosaveElement = doc.getRoot().getChild("autosave");
      ret.autosave.intervalInSeconds = Integer.parseInt(autosaveElement.getAttribute("intervalInMinutes")) * 60;
      tmp = autosaveElement.getAttribute("path");
      ret.autosave.path = decodePath(tmp);

      XElement statsElement = doc.getRoot().getChild("stats");
      ret.stats.snapshotIntervalDistance = Integer.parseInt(statsElement.getAttribute("snapshotIntervalDistance"));
    }

    return ret;
  }

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
  public AutoSave autosave = new AutoSave();
  @XmlIgnored
  private FlightStripSettings flightStripSettings = null;
  public boolean verboseXmlOperations;
  public Path logFolder;
  public Radar radar = new Radar();
  public Path soundFolder;
  public Path speechFormatterFile;
  public Path startupSettingsFile;
  public Stats stats = new Stats();
  public Path stripSettingsFile;

  private AppSettings() {
    if (!initialized)
      throw new EApplicationException("Unable to create a new instance of AppSettings. Static method 'AppSettings.init()' must be called first.");

    this.startupSettingsFile = getUnderAppFolder("defaultStartupSettings.ss.xml");
    this.soundFolder = getUnderAppFolder("_Sounds");
    this.logFolder = getUnderAppFolder("_Log");
    this.stripSettingsFile = getUnderAppFolder("stripSettings.at.xml");
    this.radar.styleSettingsFile = getUnderAppFolder("radarStyleSettings.at.xml");
  }

  public FlightStripSettings getLoadedFlightStripSettings() {
    if (flightStripSettings == null) {
      XmlSerializer ser = XmlSerializationFactory.createForFlightStripSettings();
      try {
        this.flightStripSettings = XmlSerialization.loadFromFile(ser, this.stripSettingsFile.toString(), FlightStripSettings.class);
      } catch (Exception ex) {
        throw new EApplicationException("Failed to load flight-strip-settings from " + this.stripSettingsFile.toString(), ex);
      }
    }
    return this.flightStripSettings;
  }
}