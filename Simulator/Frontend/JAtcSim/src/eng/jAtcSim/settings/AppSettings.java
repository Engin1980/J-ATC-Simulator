package eng.jAtcSim.settings;

import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.abstractRadar.settings.RadarDisplaySettings;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppSettings implements IXPersistable {

  private static Path applicationFolder;
  private static boolean initialized = false;
  private static java.time.LocalDateTime startDateTime;

  public static AppSettings loadOrCreate() {
    Path appPath = tryGetAppSettingsFile();
    if (appPath == null)
      return new AppSettings();

    XLoadContext ctx = new XLoadContext().withDefaultParsers();
    ctx.setParser(Path.class, q -> Paths.get(q));

    AppSettings ret;
    try {
      ret = ctx.loadObject(XDocument.load(appPath).getRoot(), AppSettings.class);
    } catch (EXmlException e) {
      throw new EApplicationException("Failed to load app settings"); //TODO write here only info to log and continue with default settings
    }

    ret.startupSettingsFile = decodePath(ret.startupSettingsFile.toString());
    ret.soundFolder = decodePath(ret.soundFolder.toString());
    ret.logFolder = decodePath(ret.logFolder.toString());
    ret.stripSettingsFile = decodePath(ret.stripSettingsFile.toString());
    ret.speechFormatterFile = decodePath(ret.speechFormatterFile.toString());
    ret.radar.styleSettingsFile = decodePath(ret.radar.styleSettingsFile.toString());
    ret.autosave.path = decodePath(ret.autosave.path.toString());

    ret.loadInternalStuff();

    return ret;
  }

  //TODEL
//  public static AppSettings create() {
//    AppSettings ret = new AppSettings();
//
//    Path appPath = tryGetAppSettingsFile();
//    if (appPath != null) {
//      XDocument doc;
//      try {
//        doc = XDocument.load(appPath);
//      } catch (EXmlException ex) {
//        throw new EApplicationException("App settings file was not loaded.", ex);
//      }
//
//      String tmp;
//
//      tmp = doc.getRoot().getChild("startupSettingsFile").getContent();
//      ret.startupSettingsFile = decodePath(tmp);
//
//      tmp = doc.getRoot().getChild("soundFolder").getContent();
//      ret.soundFolder = decodePath(tmp);
//
//      tmp = doc.getRoot().getChild("logFolder").getContent();
//      ret.logFolder = decodePath(tmp);
//
//      tmp = doc.getRoot().getChild("stripSettingsFile").getContent();
//      ret.stripSettingsFile = decodePath(tmp);
//
//      tmp = doc.getRoot().getChild("speechFormatterFile").getContent();
//      ret.speechFormatterFile = decodePath(tmp);
//
//      XElement radarElement = doc.getRoot().getChild("radar");
//      tmp = radarElement.getChild("styleSettingsFile").getContent();
//      ret.appRadarSettings.styleSettingsFile = decodePath(tmp);
//      ret.appRadarSettings.displayTextDelay = Integer.parseInt(radarElement.getChild("displayTextDelay").getContent());
//      ret.appRadarSettings.displaySettings = AppRadarSettings.DisplaySettings.load(radarElement.getChild("displaySettings"));
//
//      XElement autosaveElement = doc.getRoot().getChild("autosave");
//      ret.autosave.intervalInSeconds = Integer.parseInt(autosaveElement.getAttribute("intervalInMinutes")) * 60;
//      tmp = autosaveElement.getAttribute("path");
//      ret.autosave.path = decodePath(tmp);
//
//      XElement statsElement = doc.getRoot().getChild("stats");
//      ret.stats.snapshotIntervalDistance = Integer.parseInt(statsElement.getAttribute("snapshotIntervalDistance"));
//
//      ret.loadInternalStuff();
//    }
//
//    return ret;
//  }

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


  public AppAutoSaveSettings autosave = new AppAutoSaveSettings();
  @XIgnored private FlightStripSettings flightStripSettings = null;
  public boolean verboseXmlOperations;
  public Path logFolder;
  public AppRadarSettings radar = new AppRadarSettings();
  public Path soundFolder;
  public Path speechFormatterFile;
  public Path startupSettingsFile;
  @XIgnored public AppStatsSettings stats = new AppStatsSettings();
  private Path stripSettingsFile;
  @XIgnored private RadarDisplaySettings radarDisplaySettings;
  @XIgnored private RadarStyleSettings radarStyleSettings;
  @XIgnored
  private DynamicPlaneFormatter dynamicPlaneFormatter;

  @XConstructor
  private AppSettings() {
    if (!initialized)
      throw new EApplicationException("Unable to create a new instance of AppSettings. Static method 'AppSettings.init()' must be called first.");

    this.startupSettingsFile = getUnderAppFolder("defaultStartupSettings.ss.xml");
    this.soundFolder = getUnderAppFolder("_Sounds");
    this.logFolder = getUnderAppFolder("_Log");
    this.stripSettingsFile = getUnderAppFolder("stripSettings.at.xml");
    this.radar.styleSettingsFile = getUnderAppFolder("radarStyleSettings.at.xml");
  }

  public FlightStripSettings getFlightStripSettings() {
    return flightStripSettings;
  }

  public RadarDisplaySettings getRadarDisplaySettings() {
    return radarDisplaySettings;
  }

  public RadarStyleSettings getRadarStyleSettings() {
    return radarStyleSettings;
  }

  public DynamicPlaneFormatter getDynamicPlaneFormatter() {
    return dynamicPlaneFormatter;
  }

  private void loadInternalStuff() {
    this.radarStyleSettings = RadarStyleSettings.load(this.radar.styleSettingsFile);
    this.dynamicPlaneFormatter = DynamicPlaneFormatter.load(this.speechFormatterFile);
    this.flightStripSettings = FlightStripSettings.load(this.stripSettingsFile);
    this.radarDisplaySettings = this.radar.displaySettings.toRadarDisplaySettings();
  }
}
