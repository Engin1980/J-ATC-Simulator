package eng.jAtcSim.app.startupSettings.panels;

import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ExceptionUtils;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.shared.BackgroundWorker;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.shared.MessageBox;
import eng.jAtcSim.app.startupSettings.StartupSettings;
import eng.jAtcSim.app.extenders.SwingFactory;
import eng.jAtcSim.app.extenders.XmlFileSelectorExtender;

import javax.swing.*;
import javax.swing.border.TitledBorder;

public class FilesPanel extends JStartupPanel {

  private final XmlFileSelectorExtender fleArea;
  private final XmlFileSelectorExtender fleFleet;
  private final XmlFileSelectorExtender fleTypes;
  private final XmlFileSelectorExtender fleTraffic;
  private final XmlFileSelectorExtender fleWeather;
  private final JButton btnLoad;
  private static final String LOAD_FILES_LABEL = "Load XML files";
  private static final String LOADING_FILES_LABEL = "...loading";

  public FilesPanel() {
    fleArea = new XmlFileSelectorExtender(SwingFactory.FileDialogType.area);
    fleFleet = new XmlFileSelectorExtender(SwingFactory.FileDialogType.fleets);
    fleTypes = new XmlFileSelectorExtender(SwingFactory.FileDialogType.types);
    fleTraffic = new XmlFileSelectorExtender(SwingFactory.FileDialogType.traffic);
    fleWeather = new XmlFileSelectorExtender(SwingFactory.FileDialogType.weather);
    btnLoad = new JButton(LOAD_FILES_LABEL);

    this.setBorder(new TitledBorder("Source XML files:"));
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    this.add(
        LayoutManager.createFormPanel(5, 3,
            new JLabel("Area XML file:"),
            fleArea.getTextControl(),
            fleArea.getButtonControl(),
            new JLabel("Company fleets XML file:"),
            fleFleet.getTextControl(),
            fleFleet.getButtonControl(),
            new JLabel("Plane types XML file:"),
            fleTypes.getTextControl(),
            fleTypes.getButtonControl(),
            new JLabel("Traffic XML file:"),
            fleTraffic.getTextControl(),
            fleTraffic.getButtonControl(),
            new JLabel("Weather XML file:"),
            fleWeather.getTextControl(),
            fleWeather.getButtonControl()
        ));


    btnLoad.addActionListener(q -> {
      btnLoad.setText(LOADING_FILES_LABEL);
      btnLoad.setEnabled(false);
      BackgroundWorker<Object> bw = new BackgroundWorker<>(this::xmlFilesLoading, this::xmlFilesLoaded);
      bw.start();
    });
    this.add(btnLoad);
  }

  private Object xmlFilesLoading() {
    if (!loadArea()) return new Object();
    if (!loadFleet()) return new Object();
    if (!loadTypes()) return new Object();
    if (!loadTraffic()) return new Object();
    return new Object();
  }

  private void xmlFilesLoaded(Object res, Exception ex) {
    btnLoad.setText(LOAD_FILES_LABEL);
    btnLoad.setEnabled(true);
  }

  @Override
  public void fillBySettings(StartupSettings settings) {
    this.fleArea.setFileName(settings.files.areaXmlFile);
    this.fleTypes.setFileName(settings.files.planesXmlFile);
    this.fleFleet.setFileName(settings.files.fleetsXmlFile);
    this.fleTraffic.setFileName(settings.files.trafficXmlFile);
  }

  @Override
  public void fillSettingsBy(StartupSettings settings) {
    settings.files.areaXmlFile = this.fleArea.getFileName();
    settings.files.planesXmlFile = this.fleTypes.getFileName();
    settings.files.fleetsXmlFile = this.fleFleet.getFileName();
    settings.files.trafficXmlFile = this.fleTraffic.getFileName();
  }

  private boolean loadTraffic() {
    boolean ret;
    String fileName = fleTraffic.getFileName();
    if (fileName == null)
      return true;
    else {
      IList<Traffic> trfc;
      try {
        trfc = XmlLoadHelper.loadTraffic(fileName);
        ret = true;
        Sources.setTraffic(trfc);
      } catch (Exception ex) {
        ex.printStackTrace();
        MessageBox.show("Unable to load traffic file " + fileName + ".\n\nReason:\n" + ExceptionUtils.toFullString(ex, "\n"),
            "Error...");
        ret = false;
      }
    }
    return ret;
  }

  private boolean loadArea() {
    boolean ret;
    String fileName = fleArea.getFileName();
    Area area;
    try {
      area = XmlLoadHelper.loadNewArea(fileName);
      ret = true;
      Sources.setArea(area);
    } catch (Exception ex) {
      ex.printStackTrace();
      MessageBox.show("Unable to load area file " + fileName + ".\n\nReason:\n" + ExceptionUtils.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }

  private boolean loadFleet() {
    boolean ret;
    String fileName = fleFleet.getFileName();

    try {
      Sources.setFleets(
          XmlLoadHelper.loadFleets(fileName));
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load company fleets file " + fileName + ".\n\nReason:\n" + ExceptionUtils.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }

  private boolean loadTypes() {
    boolean ret;
    String fileName = fleTypes.getFileName();

    try {
      Sources.setTypes(
          XmlLoadHelper.loadPlaneTypes(fileName));
      ret = true;
    } catch (Exception ex) {
      MessageBox.show("Unable to load plane types file " + fileName + ".\n\nReason:\n" + ExceptionUtils.toFullString(ex, "\n"),
          "Error...");
      ret = false;
    }
    return ret;
  }
}
