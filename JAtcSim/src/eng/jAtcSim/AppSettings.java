package eng.jAtcSim;


import eng.eSystem.eXml.XDocument;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.xmlSerialization.Settings;
import eng.eSystem.xmlSerialization.XmlIgnore;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.common.parsers.PathValueParser;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AppSettings {

  @XmlIgnore
  private static Path applicationFolder;
  @XmlIgnore
  private static boolean initialized = false;
  private Path startupSettingsFile;
  private Path soundFolder;
  private Path logFolder;
  private Path stripSettings;
  private Path radarStyleSettings;

  public static Path getApplicationFolder() {
    return applicationFolder;
  }

  public Path getStartupSettingsFile() {
    return startupSettingsFile;
  }

  public Path getSoundFolder() {
    return soundFolder;
  }

  public Path getLogFolder() {
    return logFolder;
  }

  public static void init() {
    applicationFolder = Paths.get(System.getProperty("user.dir"));
    initialized = true;
  }

  public static Path tryGetAppSettingsFile() {
    Path ret;

    ret = getUnderAppFolder("appSettings.at.xml");
    if (ret.toFile().exists() == false) {
      ret = getUnderAppFolder("_SettingFiles\\appSettings.at.xml");
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

      tmp = doc.getRoot().getChild("stripSettings").getContent();
      ret.stripSettings = decodePath(tmp);

      tmp = doc.getRoot().getChild("radarStyleSettings").getContent();
      ret.radarStyleSettings = decodePath(tmp);

    }

    return ret;
  }

  private static Path decodePath(String tmp) {
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

  private AppSettings() {
    if (!initialized)
      throw new EApplicationException("Unable to create a new instance of AppSettings. Static method 'AppSettings.init()' must be called first.");

    this.startupSettingsFile = getUnderAppFolder("defaultStartupSettings.ss.xml");
    this.soundFolder = getUnderAppFolder("_Sounds");
    this.logFolder = getUnderAppFolder("_Log");
    this.stripSettings = getUnderAppFolder("stripSettings.at.xml");
    this.radarStyleSettings = getUnderAppFolder("radarStyleSettings.at.xml");
  }

  public Path getStripSettings() {
    return stripSettings;
  }

  public Path getRadarStyleSettings() {
    return radarStyleSettings;
  }
}