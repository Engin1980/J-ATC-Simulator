package eng.jAtcSim.app.startupSettings;

import eng.eSystem.collections.IList;
import eng.eSystem.utilites.ExceptionUtils;
import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.app.extenders.swingFactory.FileHistoryManager;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.global.logging.ApplicationLog;
import eng.jAtcSim.newLib.global.newSources.*;
import eng.jAtcSim.newLib.traffic.FlightListTraffic;
import eng.jAtcSim.newLib.traffic.Traffic;
import eng.eSystem.swing.LayoutManager;
import eng.jAtcSim.app.extenders.swingFactory.SwingFactory;
import eng.jAtcSim.app.startupSettings.panels.*;
import eng.jAtcSim.shared.MessageBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class FrmStartupSettings extends JPanel {
  private JPanel pnlContent;
  private boolean dialogResultOk;
  private String lastStartupSettingsFileName = null;
  private JButton btnValidate;

  public FrmStartupSettings() throws HeadlessException {

    // content
    pnlContent = createContentPanel();

    // bottom
    JPanel pnlBottom = createBottomPanel();

    LayoutManager.fillBorderedPanel(this, null, pnlBottom, null, null, pnlContent);
  }

  public boolean isDialogResultOk() {
    return dialogResultOk;
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

  private void validateSources() {
    StartupSettings ss = new StartupSettings();
    this.fillSettingsBy(ss);

    btnValidate.setEnabled(false);
    AirplaneTypesSource types = new AirplaneTypesSource(ss.files.planesXmlFile);
    FleetsSource fleets = new FleetsSource(ss.files.fleetsXmlFile);
    try {
      types.init();
    } catch (Exception ex) {
      Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to load types from '%s'. '%s'", ss.files.planesXmlFile,
          ExceptionUtils.toFullString(ex));
      MessageBox.show("Failed to load types from file " + ss.files.planesXmlFile + ". " + ex.getMessage(), "Error...");
      btnValidate.setEnabled(true);
      return;
    }
    try {
      fleets.init(types.getContent());
    } catch (Exception ex) {
      Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to load fleets from '%s'. '%s'", ss.files.fleetsXmlFile,
          ExceptionUtils.toFullString(ex));
      MessageBox.show("Failed to load fleets from file " + ss.files.fleetsXmlFile + ". " + ex.getMessage(), "Error...");
      btnValidate.setEnabled(true);
      return;
    }

    if (ss.weather.type == StartupSettings.Weather.WeatherSourceType.xml){
      WeatherSource ws = new XmlWeatherSource(ss.files.weatherXmlFile);
      try{
        ws.init();
      } catch (Exception ex){
        Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to load weather from '%s'. '%s'", ss.files.weatherXmlFile,
            ExceptionUtils.toFullString(ex));
        MessageBox.show("Failed to load weather from file " + ss.files.weatherXmlFile + ". " + ex.getMessage(), "Error...");
        btnValidate.setEnabled(true);
        return;
      }
    }

    if (ss.traffic.type == StartupSettings.Traffic.eTrafficType.xml) {
      TrafficSource traffics = new XmlTrafficSource(ss.files.trafficXmlFile);
      try {
        traffics.init();
      } catch (Exception ex) {
        Acc.log().writeLine(ApplicationLog.eType.warning, "Failed to load traffic from '%s'. '%s'", ss.files.trafficXmlFile,
            ExceptionUtils.toFullString(ex));
        MessageBox.show("Failed to load traffic from file " + ss.files.trafficXmlFile + ". " + ex.getMessage(), "Error...");
        btnValidate.setEnabled(true);
        return;
      }

      Traffic trf = traffics.getContent();
      if (trf instanceof FlightListTraffic) {
        boolean someFail = false;
        IList<String> requiredPlaneTypes = ((FlightListTraffic) trf).getRequiredPlaneTypes();

        for (String requiredPlaneType : requiredPlaneTypes) {
          if (types.getContent().tryGetByName(requiredPlaneType) == null) {
            Acc.log().writeLine(ApplicationLog.eType.warning, "Required plane kind '%s' not found in known plane types.", requiredPlaneType);
            someFail = true;
          }
        }
        if (someFail) {
          MessageBox.show("Some airplane types required by the traffic file are missing.", "Error...");
          btnValidate.setEnabled(true);
          return;
        }
      }
    }

    MessageBox.show("Everything seems to be ok.", "Validation successful.");
    btnValidate.setEnabled(true);
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
    sett = XmlLoadHelper.loadStartupSettings(file.getAbsolutePath());
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

    XmlLoadHelper.saveStartupSettings(sett, fileName);
    FileHistoryManager.updateHistory(SwingFactory.FileDialogType.startupSettings.toString(), fileName);
    this.lastStartupSettingsFileName = fileName;
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
}
