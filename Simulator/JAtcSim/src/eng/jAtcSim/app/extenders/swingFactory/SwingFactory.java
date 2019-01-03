package eng.jAtcSim.app.extenders.swingFactory;

import eng.eSystem.Tuple;
import eng.eSystem.collections.EList;
import eng.eSystem.collections.EMap;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.swing.other.HistoryForJFileChooser;
import eng.eSystem.swing.other.JFileChooserAsidePanel;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.XmlSettings;
import eng.jAtcSim.frmPacks.shared.MoodHistoryPanel;

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
import java.nio.file.StandardCopyOption;
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
  public static final String WEATHER_EXTENSION = ".we.xml";

  public static JScrollBar createHorizontalBar(int minimum, int maximum, int value) {
    JScrollBar ret = new JScrollBar(JScrollBar.HORIZONTAL);
    ret.getModel().setRangeProperties(value, 0, minimum, maximum, true);
    return ret;
  }

  private static void bindAsidePanel(JFileChooser jFileChooser, JFileChooserAsidePanel... asidePanels) {
    JFileChooserAsidePanel.LayoutDefinition layoutDefinition =
        new JFileChooserAsidePanel.LayoutDefinition(JFileChooserAsidePanel.eOrientation.vertical, 500, 500);
    JFileChooserAsidePanel.bind(jFileChooser, layoutDefinition, asidePanels);
  }

  public static JFileChooser createFileDialog(FileDialogType type, String defaultFile) {
    JFileChooser ret = new JFileChooser();
    HistoryForJFileChooser historyPanel;

    ret.setAcceptAllFileFilterUsed(false);
    switch (type) {
      case area:
        ret.addChoosableFileFilter(new FileTypeFilter(AREA_FILE_EXTENSION, "area files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case fleets:
        ret.addChoosableFileFilter(new FileTypeFilter(FLEET_FILE_EXTENSION, "Fleet files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case startupSettings:
        ret.addChoosableFileFilter(new FileTypeFilter(STARTUP_SETTING_FILE_EXTENSION, "Startup settings"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case game:
        ret.addChoosableFileFilter(new FileTypeFilter(SAVED_SIMULATION_EXTENSION, "Saved simulation"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel);
        //TODO make the support of meta here: ret.setAccessory(new XmlMetaInfoPreview(ret));
        break;
      case traffic:
        ret.addChoosableFileFilter(new FileTypeFilter(TRAFFIC_FILE_EXTENSION, "Traffic files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case types:
        ret.addChoosableFileFilter(new FileTypeFilter(AIRPLANE_TYPES_EXTENSION, "Airplane types files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case weather:
        ret.addChoosableFileFilter(new FileTypeFilter(WEATHER_EXTENSION, "Weather preset files"));
        ret.addChoosableFileFilter(new FileTypeFilter(".xml", "XML files"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel, new XmlMetaInfoPreviewAside());
        break;
      case folder:
        ret.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel);
        break;
      case ffmpeg:
        ret.addChoosableFileFilter(new FileTypeFilter("ffmpeg.exe", "ffmpeg.exe"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel);
        break;
      case video:
        ret.addChoosableFileFilter(new FileTypeFilter(".mp4", "MP4 video file"));
        historyPanel = FileHistoryManager.getAsidePanel(type.toString());
        bindAsidePanel(ret, historyPanel);
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

  public static void show(JPanel pnl, String title) {
    JFrame frm = getAsFrame(pnl, title);
    frm.setVisible(true);
  }

  public static JFrame getAsFrame(JPanel pnl, String title) {
    JFrame ret = new JFrame();
    ret.getContentPane().add(pnl);
    ret.pack();
    ret.setTitle(title);
    return ret;
  }

  public static void showDialog(JPanel pnl, String title, JFrame owner) {
    JDialog dlg = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
    dlg.getContentPane().add(pnl);
    dlg.pack();
    dlg.setLocationRelativeTo(null);
    dlg.setTitle(title);
    dlg.setVisible(true);
  }

  public static void showDialog(JPanel pnl, String title, JDialog owner) {
    JDialog dlg = new JDialog(owner, Dialog.ModalityType.DOCUMENT_MODAL);
    dlg.getContentPane().add(pnl);
    dlg.pack();
    dlg.setLocationRelativeTo(null);
    dlg.setTitle(title);
    dlg.setVisible(true);
  }

  public static JButton createButton(String title, ActionListener action) {
    JButton ret = new JButton(title);
    ret.addActionListener(action);
    return ret;
  }
}


