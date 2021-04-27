package eng.jAtcSim.app.extenders.swingFactory;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.EXmlException;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.swing.other.HistoryForJFileChooser;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

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
    try {
      doc = XDocument.load(userHomeHistoryFile.toAbsolutePath().toString());
    } catch (Exception ex) {
      throw new ApplicationException("Failed to load history of loaded files.", ex);
    }

//    XmlContext ctx = new XmlContext();
//    ctx.sdfManager.setDeserializer(EMap.class, new EntriesDeserializer());
//    ctx.sdfManager.setDeserializer(EList.class, new ItemsDeserializer());
//    ctx.sdfManager.setDeserializer(String.class, (e, c) -> e.getContent());
//    IMap<String, IList<String>> tmp = (IMap<String, IList<String>>) XmlContext.deserialize(doc.getRoot(), ctx, EMap.class);
//
//    FileHistoryManager.histories = tmp;


    XLoadContext ctx = new XLoadContext();
    ctx.setParser(String.class, s -> s);
    ctx.setDeserializer(EList.class, e -> ctx.objects.loadItems(e, String.class));
    ctx.setDeserializer(EMap.class, e -> ctx.objects.loadEntries(e, String.class, String.class));

    IMap<String, IList<String>> tmp = (IMap<String, IList<String>>) ctx.loadObject(doc.getRoot(), EMap.class);
    FileHistoryManager.histories = tmp;

//    XLoadContext ctx = new XLoadContext();
//    ctx.setDeserializer(IMap.class, e -> {
//      EMap<String, IList<String>> ret = new EMap<>();
//      for (XElement se : e.getChildren()) {
//        String key = se.getName();
//        IList<String> values = new EList<>();
//        ctx.loadItems(e, values, String.class);
//        ret.set(key, values);
//      }
//      return ret;
//    });
//    ctx.setDeserializer(IList.class, e -> {
//      IList<String> ret = new EList<>();
//      for (XElement se : e.getChildren()) {
//        String s = se.getContent();
//        ret.add(s);
//      }
//      return ret;
//    });
//
//    FileHistoryManager.histories = (IMap<String, IList<String>>) ctx.loadObject(doc.getRoot(), IMap.class);
  }

  private static void saveHistories() {
    Path userHomeHistoryFile = getHistoryFilePath();

    XElement root = new XElement("root");

    XSaveContext ctx = new XSaveContext();
    ctx.setSerializer(EMap.class, (o, e) -> ctx.objects.saveEntries(o, String.class, IList.class, e));
    ctx.setSerializer(EList.class, (o, e) -> ctx.objects.saveItems(o, String.class, e));
    ctx.setFormatter(String.class, s -> s);

    ctx.saveObject(FileHistoryManager.histories, root);

    XDocument doc = new XDocument(root);

    Path tmpFile;
    try {
      tmpFile = java.nio.file.Files.createTempFile("", "");
      doc.save(tmpFile);
    } catch (IOException | EXmlException e) {
      throw new ApplicationException("Failed to create a temp xml file '%s'.", e);
    }
    try {
      java.nio.file.Files.copy(tmpFile, userHomeHistoryFile, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new ApplicationException("Failed to replace existing history file.", e);
    }
  }
}
