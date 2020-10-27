package eng.jAtcSim.app.extenders.swingFactory;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.exceptions.EXmlException;
import eng.eSystem.swing.other.HistoryForJFileChooser;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;
import eng.jAtcSimLib.xmlUtils.serializers.EntriesWithListValuesSerializer;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileHistoryManager {
  private static final Dimension defaultDimension = new Dimension(500, 100);
  private static IMap<String, IList<String>> histories = new EMap<>();
  private static boolean initialized = false;

  public static HistoryForJFileChooser getAsidePanel(String key) {
    if (initialized == false)
      init();
    HistoryForJFileChooser ret = new HistoryForJFileChooser(defaultDimension);
    IList<String> hs = histories.getOrSet(key, new EList<>());
    IList<Path> hp = hs.select(q -> Paths.get(q));
    ret.setHistory(hp);
    return ret;
  }

  public static void init() {
    loadHistories();
    initialized = true;
  }

  public static void updateHistory(String key, String path) {
    IList<String> lst = histories.getOrSet(key, new EList<>());
    if (lst.contains(path))
      lst.remove(path);
    lst.insert(0, path);
    saveHistories();
  }

  private static Path getHistoryFilePath() {
    String userHome = System.getProperty("user.home");
    Path ret = Paths.get(userHome, "jAtcSimFileHistory.xml");
    return ret;
  }

  private static void loadHistories() {
    Path userHomeHistoryFile = getHistoryFilePath();

    if (java.nio.file.Files.exists(userHomeHistoryFile) == false)
      return;


    XDocument doc;
    IMap<String, IList<String>> tmp = new EMap<>();
    try {
      doc = XDocument.load(userHomeHistoryFile.toAbsolutePath().toString());
    } catch (Exception ex) {
      throw new EApplicationException("Failed to load history of loaded files.", ex);
    }

    XmlLoaderUtils.loadMap(doc.getRoot(), tmp,
            e -> e.getAttribute("key"),
            e -> XmlLoaderUtils.loadList(e, new EList<String>(),
                    f -> f.getContent()));

    FileHistoryManager.histories = tmp;
  }

  private static void saveHistories() {
    Path userHomeHistoryFile = getHistoryFilePath();

    XElement root = XmlSaveUtils.Entries.IListValues.saveAsElement(
            "root",
            histories,
            new EntriesWithListValuesSerializer<>(
                    (e, q) -> e.setContent(q),
                    (e, q) -> e.setContent(q)));

    XDocument doc = new XDocument(root);

    Path tmpFile;
    try {
      tmpFile = java.nio.file.Files.createTempFile("", "");
      doc.save(tmpFile);
    } catch (IOException | EXmlException e) {
      throw new ERuntimeException("Failed to create a temp xml file '%s'.", e);
    }
    try {
      java.nio.file.Files.copy(tmpFile, userHomeHistoryFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ERuntimeException("Failed to replace existing history file.", e);
    }
  }
}
