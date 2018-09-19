package eng.jAtcSim.app.extenders;

import eng.eSystem.Tuple;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.shared.LayoutManager;

import javax.swing.*;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class SwingFactory {

  public enum FileDialogType {
    area,
    fleets,
    types,
    traffic,
    game,
    startupSettings,
    folder,
    ffmpeg,
    weather, video
  }

  public static final String AREA_FILE_EXTENSION = ".ar.xml";
  public static final String FLEET_FILE_EXTENSION = ".fl.xml";
  public static final String STARTUP_SETTING_FILE_EXTENSION = ".ss.xml";
  public static final String SAVED_SIMULATION_EXTENSION = ".sm.xml";
  public static final String TRAFFIC_FILE_EXTENSION = ".tr.xml";
  public static final String AIRPLANE_TYPES_EXTENSION = ".tp.xml";

  public static JScrollBar createHorizontalBar(int minimum, int maximum, int value) {
    JScrollBar ret = new JScrollBar(JScrollBar.HORIZONTAL);
    ret.getModel().setRangeProperties(value, 0, minimum, maximum, true);
    return ret;
  }

  public static JFileChooser createFileDialog(FileDialogType type, String defaultFile) {
    JFileChooser ret = new JFileChooser();

    ret.setAcceptAllFileFilterUsed(false);
    switch (type) {
      case area:
        ret.addChoosableFileFilter(new FileTypeFilter(AREA_FILE_EXTENSION, "area files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case fleets:
        ret.addChoosableFileFilter(new FileTypeFilter(FLEET_FILE_EXTENSION, "Fleet files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case startupSettings:
        ret.addChoosableFileFilter(new FileTypeFilter(STARTUP_SETTING_FILE_EXTENSION, "Startup settings"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case game:
        ret.addChoosableFileFilter(new FileTypeFilter(SAVED_SIMULATION_EXTENSION, "Saved simulation"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        //TODO make the support of meta here: ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case traffic:
        ret.addChoosableFileFilter(new FileTypeFilter(TRAFFIC_FILE_EXTENSION, "Traffic files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case types:
        ret.addChoosableFileFilter(new FileTypeFilter(AIRPLANE_TYPES_EXTENSION, "Airplane types files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case folder:
        ret.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        break;
      case ffmpeg:
        ret.addChoosableFileFilter(new FileTypeFilter("ffmpeg.exe", "ffmpeg.exe"));
        break;
      case video:
        ret.addChoosableFileFilter(new FileTypeFilter(".mp4", "MP4 video file"));
        break;
      default:
        throw new EEnumValueUnsupportedException(type);
    }
    ret.addChoosableFileFilter(new FileTypeFilter("", "All files"));

    if (defaultFile == null)
      ret.setCurrentDirectory(new File(System.getProperty("user.home")));
    else {
      Path p = Paths.get(defaultFile);
      ret.setSelectedFile(p.toFile());
      p = p.getParent();
      ret.setCurrentDirectory(p.toFile());
    }

    return ret;
  }

  public static void show(JPanel pnl, String title){
    JFrame frm = getAsFrame(pnl, title);
    frm.setVisible(true);
  }

  public static JFrame getAsFrame(JPanel pnl, String title){
    JFrame ret = new JFrame();
    ret.getContentPane().add(pnl);
    ret.pack();
    ret.setTitle(title);
    return ret;
  }

  public static void showDialog(JPanel pnl, String title, JFrame owner){
    JDialog dlg = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
    dlg.getContentPane().add(pnl);
    dlg.pack();
    dlg.setLocationRelativeTo(null);
    dlg.setTitle(title);
    dlg.setVisible(true);
  }
  public static void showDialog(JPanel pnl, String title, JDialog owner){
    JDialog dlg = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
    dlg.getContentPane().add(pnl);
    dlg.pack();
    dlg.setLocationRelativeTo(null);
    dlg.setTitle(title);
    dlg.setVisible(true);
  }

  public static JButton createButton(String title, ActionListener action){
    JButton ret = new JButton(title);
    ret.addActionListener(action);
    return ret;
  }
}


class FileTypeFilter extends FileFilter {
  private String extension;
  private String description;

  public FileTypeFilter(String extension, String description) {
    this.extension = extension;
    this.description = description;
  }

  public boolean accept(File file) {
    if (file.isDirectory()) {
      return true;
    }
    return file.getName().endsWith(extension);
  }

  public String getDescription() {
    return description + String.format(" (*%s)", extension);
  }
}

class XmlMetaInfoPreview extends JComponent implements PropertyChangeListener {
  final static int WIDTH = 300;
  final static int HEIGHT = 300;
  JLabel txtTitle = new JLabel();
  JLabel txtAuthor = new JLabel();
  JLabel txtVersionDate = new JLabel();
  JTextArea txtDescription = new JTextArea();

  public XmlMetaInfoPreview(JFileChooser fc) {
    fc.addPropertyChangeListener(this);

    txtAuthor.setFont(txtDescription.getFont());
    txtVersionDate.setFont(txtDescription.getFont());
    txtDescription.setLineWrap(true);
    txtDescription.setWrapStyleWord(true);
    txtDescription.setBackground(txtAuthor.getBackground());
    txtDescription.setEditable(false);


    JPanel pnlTitle = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 0, txtTitle);
    JPanel pnlVersionDate = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 0, txtVersionDate);
    JPanel pnlAuthor = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 0, txtAuthor);
    JScrollPane pnlDescriptionScroll =new JScrollPane(txtDescription);
    pnlDescriptionScroll.setBorder(null);
    JPanel pnlDescription = LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.baseline, 0,
        pnlDescriptionScroll);

    JPanel pnlAll = LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.left, 8,
        pnlTitle, pnlVersionDate, pnlAuthor, pnlDescription);
    pnlAll.setBorder(new TitledBorder("File META info:"));
    LayoutManager.fillBorderedPanel(this, 8, pnlAll);
    this.setPreferredSize(new Dimension(WIDTH, HEIGHT));

    fc.setAccessory(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent e) {
    String propName = e.getPropertyName();

    if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(propName)) {
      clearContent();
      return;
    }

    if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(propName)) {

      File file = (File) e.getNewValue();

      if (file == null) {
        clearContent();
        return;
      } else {
        tryFillContent(file);
      }
    }
  }

  private void tryFillContent(File file) {
    if (file.getName().endsWith("xml")) {
      Tuple<XmlInfo, Exception> xmlInfo = tryReadXmlMeta(file);
      if (xmlInfo.getA() != null) {
        txtTitle.setText(xmlInfo.getA().title);
        txtVersionDate.setText(xmlInfo.getA().version + " " + xmlInfo.getA().date);
        txtAuthor.setText("by " + xmlInfo.getA().author);
        txtDescription.setText(xmlInfo.getA().description);
      } else if (xmlInfo.getB() != null) {
        txtDescription.setText("Error reading file: " + xmlInfo.getB().getMessage());
      } else {
        clearContent();
        txtVersionDate.setText("No META info found.");
      }
    } else
      clearContent();
  }

  private Tuple<XmlInfo, Exception> tryReadXmlMeta(File file) {
    Tuple<String, Exception> meta = tryExtractMeta(file);
    if (meta.getB() != null)
      return new Tuple<>(null, meta.getB());
    else if (meta.getA() == null)
      return new Tuple<>(null, null);
    else {
      XmlInfo ret = new XmlInfo();
      ret.author = tryExtract(meta.getA(), "author");
      ret.title = tryExtract(meta.getA(), "title");
      ret.date = tryExtract(meta.getA(), "date");
      ret.version = tryExtract(meta.getA(), "version");
      ret.description = tryExtract(meta.getA(), "description");
      ret.link = tryExtract(meta.getA(), "link");
      return new Tuple<>(ret, null);
    }
  }

  private String tryExtract(String data, String elementName) {
    String ret;
    String patternText = sf("<%s>(.*)<\\/%s>", elementName, elementName);
    Pattern p = Pattern.compile(patternText, Pattern.DOTALL);
    Matcher m = p.matcher(data);
    if (m.find())
      ret = m.group(1).trim();
    else ret = "";
    return ret;
  }

  private Tuple<String, Exception> tryExtractMeta(File file) {
    Tuple<String, Exception> ret;

    String lookFor;
    int fromIndex = -1;
    int toIndex = -1;
    final int maxRange = 500;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      lookFor = "<meta>";
      fromIndex = lookFor(lookFor, maxRange, br);
      if (fromIndex < 0)
        return new Tuple<>(null, null);

      lookFor = "</meta>";
      toIndex = lookFor(lookFor, Integer.MAX_VALUE, br);
      if (toIndex < 0)
        return new Tuple<>(null, null);
      else
        toIndex += fromIndex - lookFor.length();
    } catch (Exception ex) {
      ret = new Tuple<>(null, ex);
      return ret;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      StringBuilder sb = new StringBuilder();
      int index = 0;
      while (index < toIndex) {
        char c = (char) br.read();
        if (index > fromIndex)
          sb.append(c);
        index++;
      }

      ret = new Tuple<>(sb.toString(), null);

    } catch (Exception ex) {
      ret = new Tuple<>(null, ex);
      return ret;
    }
    return ret;
  }

  private int lookFor(String lookFor, int maxValue, BufferedReader br) throws IOException {
    int ret = 0;
    int matchIndex = 0;

    int m = lookFor.charAt(matchIndex);
    int c;
    while ((c = br.read()) > 0) {
      ret++;
      if (ret > maxValue)
        break;
      if (m == c) {
        matchIndex++;
        if (matchIndex == lookFor.length())
          return ret;
        else {
          m = lookFor.charAt(matchIndex);
        }
      } else if (matchIndex > 0) {
        matchIndex = 0;
        m = lookFor.charAt(matchIndex);
      }
    }
    return -1;
  }

  private void clearContent() {
    this.txtAuthor.setText("");
    this.txtTitle.setText("");
    this.txtDescription.setText("Choose XML source file.");
    this.txtVersionDate.setText("");
  }
}

class XmlInfo {
  public String author;
  public String date;
  public String title;
  public String version;
  public String description;
  public String link;
}
