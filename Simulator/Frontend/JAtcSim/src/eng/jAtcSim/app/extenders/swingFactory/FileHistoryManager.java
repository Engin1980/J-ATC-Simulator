package eng.jAtcSim.app.extenders.swingFactory;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.swing.other.HistoryForJFileChooser;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileHistoryManager {
  private static Dimension defaultDimension = new Dimension(500, 100);
  private static EMap<String, EList<String>> histories = new eng.eSystem.collections.EMap<>();
  private static boolean initialized = false;

  public static HistoryForJFileChooser getAsidePanel(String key) {
    if (initialized == false)
      init();
    HistoryForJFileChooser ret = new HistoryForJFileChooser(defaultDimension);
    IList<String> hs = histories.getOrSet(key, new EList<>());
    IList<Path> hp = hs.select(q->Paths.get(q));
    ret.setHistory(hp);
    return ret;
  }

  public static void init() {
    loadHistories();
    initialized = true;
  }

  public static void updateHistory(String key, String path) {
    EList<String> lst = histories.getOrSet(key, new EList<>());
    if (lst.contains(path))
      lst.remove(path);
    lst.insert(0, path);
    saveHistories();
  }

  private static void loadHistories() {
    Path userHomeHistoryFile = getHistoryFilePath();

    if (java.nio.file.Files.exists(userHomeHistoryFile) == false)
      return;

    XmlSettings sett = getXmlHistorySettings();
    XmlSerializer ser = new XmlSerializer(sett);
    EMap<String, EList<String>> tmp;
    try {
      tmp = ser.deserialize(userHomeHistoryFile.toAbsolutePath().toString(), EMap.class);
      FileHistoryManager.histories = tmp;
    } catch (Exception ex) {
      throw new EApplicationException("Failed to load history of loaded files.", ex);
    }
  }

  private static void saveHistories() {
    Path userHomeHistoryFile = getHistoryFilePath();

    XmlSettings sett = getXmlHistorySettings();
    XmlSerializer ser = new XmlSerializer(sett);
    Path tmpFile;
    try {
      tmpFile = java.nio.file.Files.createTempFile("", "");
    } catch (IOException e) {
      throw new ERuntimeException("Failed to create a temp file.", e);
    }
    ser.serialize(tmpFile.toAbsolutePath().toString(), histories);
    try {
      java.nio.file.Files.copy(tmpFile, userHomeHistoryFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ERuntimeException("Failed to replace existing history file.", e);
    }
  }

  private static Path getHistoryFilePath() {
    String userHome = System.getProperty("user.home");
    Path ret = Paths.get(userHome, "jAtcSimFileHistory.xml");
    return ret;
  }

  private static XmlSettings getXmlHistorySettings() {
    XmlSettings ret = new XmlSettings();

    return ret;
  }
}
