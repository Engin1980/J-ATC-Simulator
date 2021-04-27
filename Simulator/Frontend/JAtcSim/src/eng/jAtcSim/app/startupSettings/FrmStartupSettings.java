package eng.jAtcSim.app.startupSettings;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XDocument;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.ApplicationException;
import eng.eSystem.swing.LayoutManager;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.app.extenders.swingFactory.FileHistoryManager;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.panels.*;
import eng.jAtcSim.contextLocal.Context;
import eng.jAtcSim.newLib.gameSim.game.sources.*;
import eng.jAtcSim.newLib.shared.logging.LogItemType;
import eng.jAtcSim.newLib.traffic.ITrafficModel;
import eng.jAtcSim.newLib.traffic.models.FlightListTrafficModel;
import eng.jAtcSim.shared.MessageBox;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class FrmStartupSettings extends JPanel {
  private JButton btnValidate;
  private boolean dialogResultOk;
  private String lastStartupSettingsFileName = null;
  private final JPanel pnlContent;

  public FrmStartupSettings() throws HeadlessException {

    // content
    pnlContent = createContentPanel();

    // bottom
    JPanel pnlBottom = createBottomPanel();

    LayoutManager.fillBorderedPanel(this, null, pnlBottom, null, null, pnlContent);
  }

  public void fillBySettings(StartupSettings settings) {
    ComponentUtils.adjustComponentTree(this,
            q -> q instanceof IForSettings, q -> {
              IForSettings ifs = (IForSettings) q;
              ifs.fillBySettings(settings);
            });
  }

  public void fillSettingsBy(StartupSettings settings) {
    ComponentUtils.adjustComponentTree(this,
            q -> q instanceof IForSettings, q -> {
              IForSettings ifs = (IForSettings) q;
              ifs.fillSettingsBy(settings);
            });
  }

  public boolean isDialogResultOk() {
    return dialogResultOk;
  }

  private void btnApply_click(ActionEvent actionEvent) {
    this.dialogResultOk = true;
    this.getRootPane().getParent().setVisible(false);
  }

  private void btnLoad_click() {
    JFileChooser jfc = SwingFactory.createFileDialog(SwingFactory.FileDialogType.startupSettings, lastStartupSettingsFileName);

    int res = jfc.showOpenDialog(this);
    if (res != JFileChooser.APPROVE_OPTION) return;

    File file = jfc.getSelectedFile();
    StartupSettings sett;
    try {
      XLoadContext ctx = new XLoadContext().withDefaultParsers();
      sett = ctx.loadObject(XDocument.load(file).getRoot(), StartupSettings.class);
    } catch (Exception e) {
      throw new ApplicationException("Unable to load startup settings.", e);
    }

    this.fillBySettings(sett);
    FileHistoryManager.updateHistory(SwingFactory.FileDialogType.startupSettings.toString(), file.toPath().toString());
    this.lastStartupSettingsFileName = file.getAbsolutePath();
  }

  private void btnSave_click() {
    JFileChooser jfc = SwingFactory.createFileDialog(SwingFactory.FileDialogType.startupSettings, lastStartupSettingsFileName);

    int res = jfc.showSaveDialog(this);
    if (res != JFileChooser.APPROVE_OPTION) return;

    String fileName = jfc.getSelectedFile().toString();
    if (fileName.endsWith(SwingFactory.STARTUP_SETTING_FILE_EXTENSION) == false)
      fileName += SwingFactory.STARTUP_SETTING_FILE_EXTENSION;

    StartupSettings sett = new StartupSettings();
    this.fillSettingsBy(sett);

    try {
      XSaveContext ctx = new XSaveContext().withDefaultFormatters();
      XElement root = ctx.saveObject(sett, "startupSettings");
      XDocument doc = new XDocument(root);
      doc.save(fileName);
    } catch (Exception e) {
      throw new ApplicationException("Failed to save startup settings.", e);
    }
    FileHistoryManager.updateHistory(SwingFactory.FileDialogType.startupSettings.toString(), fileName);
    this.lastStartupSettingsFileName = fileName;
  }

  private JPanel createBottomPanel() {
    btnValidate = SwingFactory.createButton("Validate", q -> validateSources());
    JButton btnSave = SwingFactory.createButton("Save", q -> btnSave_click());
    JButton btnLoad = SwingFactory.createButton("Load", q -> btnLoad_click());
    JButton btnApply = SwingFactory.createButton("Apply", this::btnApply_click);
    JButton btnCancel = SwingFactory.createButton("Discard changes", q -> {
      this.dialogResultOk = false;
      this.getRootPane().getParent().setVisible(false);
    });

    JPanel ret = LayoutManager.createBorderedPanel(
            null,
            null,
            LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 4,
                    btnLoad,
                    btnSave),
            LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 4,
                    btnCancel,
                    btnValidate,
                    btnApply),
            null);

    ret = LayoutManager.createBorderedPanel(4, ret);

    return ret;
  }

  private JPanel createContentPanel() {
    JPanel ret = new JPanel();

    JTabbedPane tabbedPane = new JTabbedPane();

    AirportAndAirplanesPanel pnlA = new AirportAndAirplanesPanel();
    tabbedPane.addTab("Airport, planes & fleets", pnlA);

    WeatherPanel pnlB = new WeatherPanel();
    tabbedPane.addTab("Weather", pnlB);
    pnlA.getOnIcaoChanged().add(pnlB::setRelativeIcao);


    TrafficPanel pnlC = new TrafficPanel();
    tabbedPane.addTab("Traffic", pnlC);

    SimulationTimeRadarSettings pnlD = new SimulationTimeRadarSettings();
    tabbedPane.addTab("Simulation", pnlD);

    ret.add(tabbedPane);

    return ret;
  }

  private void validateSources() {
    StartupSettings ss = new StartupSettings();
    this.fillSettingsBy(ss);

    btnValidate.setEnabled(false);

    AirplaneTypesSource types = SourceFactory.createAirplaneTypesSource(ss.files.planesXmlFile);
    try {
      types.init();
    } catch (Exception ex) {
      Context.getApp().getAppLog().write(LogItemType.warning, "Failed to load types from '%s'. '%s'", ss.files.planesXmlFile,
              ExceptionUtils.toFullString(ex));
      MessageBox.show("Failed to load types from file " + ss.files.planesXmlFile + ". " + ex.getMessage(), "Error...");
      btnValidate.setEnabled(true);
      return;
    }

    FleetsSource fleets = SourceFactory.createFleetsSource(ss.files.generalAviationFleetsXmlFile, ss.files.companiesFleetsXmlFile);
    try {
      fleets.init();
    } catch (Exception ex) {
      Context.getApp().getAppLog().write(LogItemType.warning, "Failed to load fleets from '%s' and/or '%s'. '%s'",
              ss.files.companiesFleetsXmlFile,
              ss.files.generalAviationFleetsXmlFile,
              ExceptionUtils.toFullString(ex));
      MessageBox.show(sf("Failed to load fleets from file '%s' and/or '%s'. %s.",
              ss.files.companiesFleetsXmlFile, ss.files.generalAviationFleetsXmlFile, ex.getMessage()),
              "Error...");
      btnValidate.setEnabled(true);
      return;
    }

    if (ss.weather.type == StartupSettings.Weather.WeatherSourceType.xml) {
      WeatherSource ws = SourceFactory.createWeatherXmlSource(ss.files.weatherXmlFile);
      try {
        ws.init();
      } catch (Exception ex) {
        Context.getApp().getAppLog().write(LogItemType.warning, "Failed to load weather from '%s'. '%s'", ss.files.weatherXmlFile,
                ExceptionUtils.toFullString(ex));
        MessageBox.show("Failed to load weather from file " + ss.files.weatherXmlFile + ". " + ex.getMessage(), "Error...");
        btnValidate.setEnabled(true);
        return;
      }
    }

    TrafficSource traffics = SourceFactory.createTrafficXmlSource(ss.files.trafficXmlFile);
    try {
      traffics.init();
    } catch (Exception ex) {
      Context.getApp().getAppLog().write(LogItemType.warning, "Failed to load traffic from '%s'. '%s'", ss.files.trafficXmlFile,
              ExceptionUtils.toFullString(ex));
      MessageBox.show("Failed to load traffic from file " + ss.files.trafficXmlFile + ". " + ex.getMessage(), "Error...");
      btnValidate.setEnabled(true);
      return;
    }

    ITrafficModel tm = traffics.getContent();
    if (tm instanceof FlightListTrafficModel) {
      FlightListTrafficModel fltm = (FlightListTrafficModel) tm;
      IReadOnlyList<String> requiredPlaneTypes = fltm.getRequiredPlaneTypes();
      IReadOnlyList<String> knownPlaneTypes = types.getContent().getTypeNames();
      IReadOnlyList<String> unknownPlaneTypes = requiredPlaneTypes
              .where(q -> knownPlaneTypes.contains(q) == false);
      for (String unknownPlaneType : unknownPlaneTypes) {
        Context.getApp().getAppLog().write(LogItemType.warning, "Required plane kind '%s' not found in known plane types.", unknownPlaneType);
      }
      if (unknownPlaneTypes.isEmpty() == false) {
        MessageBox.show("Some airplane types required by the traffic file are missing.", "Error...");
        btnValidate.setEnabled(true);
        return;
      }
    }

    MessageBox.show("Everything seems to be ok.", "Validation successful.");
    btnValidate.setEnabled(true);
  }
}
