package eng.jAtcSim.settings;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eXmlSerialization.XmlSerializer;
import eng.eXmlSerialization.annotations.XmlIgnored;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
import eng.jAtcSim.xmlLoading.XmlSerialization;
import eng.jAtcSim.xmlLoading.XmlSerializationFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class AppSettings {

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
      ret.appRadarSettings.styleSettingsFile = decodePath(tmp);
      ret.appRadarSettings.displayTextDelay = Integer.parseInt(radarElement.getChild("displayTextDelay").getContent());
      ret.appRadarSettings.displaySettings = AppRadarSettings.DisplaySettings.load(radarElement.getChild("displaySettings"));
      //) new XmlSerializer().deserialize(
      //    radarElement.getChild("displaySettings"), Radar.DisplaySettings.class);

      XElement autosaveElement = doc.getRoot().getChild("autosave");
      ret.autosave.intervalInSeconds = Integer.parseInt(autosaveElement.getAttribute("intervalInMinutes")) * 60;
      tmp = autosaveElement.getAttribute("path");
      ret.autosave.path = decodePath(tmp);

      XElement statsElement = doc.getRoot().getChild("stats");
      ret.stats.snapshotIntervalDistance = Integer.parseInt(statsElement.getAttribute("snapshotIntervalDistance"));

      ret.load();
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

  private static DynamicPlaneFormatter loadDynamicPlaneFormatter(Path speechFormatterFile) {
    IMap<Class<?>, IList<Sentence>> speechResponses;
    try {
      XmlSerializer ser = XmlSerializationFactory.createForSpeechResponses();
      speechResponses = XmlSerialization.loadFromFile(ser, speechFormatterFile.toFile(), IMap.class);
    } catch (EApplicationException ex) {
      throw new EApplicationException(
              sf("Unable to load speech responses from xml file '%s'.", speechFormatterFile), ex);
    }
    DynamicPlaneFormatter ret = new DynamicPlaneFormatter(speechResponses);
    return ret;
  }

  public AppAutoSaveSettings autosave = new AppAutoSaveSettings();
  @XmlIgnored
  private FlightStripSettings flightStripSettings = null;
  public boolean verboseXmlOperations;
  public Path logFolder;
  public AppRadarSettings appRadarSettings = new AppRadarSettings();
  public Path soundFolder;
  public Path speechFormatterFile;
  public Path startupSettingsFile;
  public AppStatsSettings stats = new AppStatsSettings();
  private Path stripSettingsFile;
  @XmlIgnored
  private RadarStyleSettings displaySettings;
  @XmlIgnored
  private RadarStyleSettings radarStyleSettings;
  @XmlIgnored
  private DynamicPlaneFormatter dynamicPlaneFormatter;

  private AppSettings() {
    if (!initialized)
      throw new EApplicationException("Unable to create a new instance of AppSettings. Static method 'AppSettings.init()' must be called first.");

    this.startupSettingsFile = getUnderAppFolder("defaultStartupSettings.ss.xml");
    this.soundFolder = getUnderAppFolder("_Sounds");
    this.logFolder = getUnderAppFolder("_Log");
    this.stripSettingsFile = getUnderAppFolder("stripSettings.at.xml");
    this.appRadarSettings.styleSettingsFile = getUnderAppFolder("radarStyleSettings.at.xml");
  }

  public FlightStripSettings getFlightStripSettings() {
    return flightStripSettings;
  }

  public void load() {
    this.radarStyleSettings = RadarStyleSettings.load(this.appRadarSettings.styleSettingsFile.toString());
    this.displaySettings = XmlLoadHelper.loadNewDisplaySettings(this.appRadarSettings.styleSettingsFile.toString());
    this.dynamicPlaneFormatter = loadDynamicPlaneFormatter(this.speechFormatterFile);
    this.flightStripSettings = loadFlightStripSettings();
  }

  private FlightStripSettings loadFlightStripSettings() {
    FlightStripSettings ret;
    XmlSerializer ser = XmlSerializationFactory.createForFlightStripSettings();
    try {
      ret = XmlSerialization.loadFromFile(ser, this.stripSettingsFile.toString(), FlightStripSettings.class);
    } catch (Exception ex) {
      throw new EApplicationException("Failed to load flight-strip-settings from " + this.stripSettingsFile.toString(), ex);
    }
    return ret;
  }
}
