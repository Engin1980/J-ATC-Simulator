package eng.jAtcSim.app.startupSettings;

import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.shared.LayoutManager;
import eng.jAtcSim.app.extenders.SwingFactory;
import eng.jAtcSim.app.startupSettings.panels.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class FrmStartupSettings extends JPanel {
  private JPanel pnlContent;
  private boolean dialogResultOk;
  private String lastStartupSettingsFileName = null;

  public FrmStartupSettings() throws HeadlessException {

    // top
    JPanel pnlTop = createTopPanel();

    // content
    pnlContent = createContentPanel();

    // bottom
    JPanel pnlBottom = createBottomPanel();

    //JPanel pnl = LayoutManager.createBorderedPanel(pnlTop, pnlBottom, null, null, pnlContent);
    LayoutManager.fillBorderedPanel(this, pnlTop, pnlBottom, null, null, pnlContent);
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

    JButton btnSave = new JButton("Save");
    JButton btnLoad = new JButton("Load");
    JButton btnApply = new JButton("Apply");
    btnApply.addActionListener(q -> {
      this.dialogResultOk = true;
      this.getRootPane().getParent().setVisible(false);
    });
    JButton btnCancel = new JButton("Discard changes");
    btnCancel.addActionListener(q -> {
      this.dialogResultOk = false;
      this.getRootPane().getParent().setVisible(false);
    });

    btnSave.addActionListener(q -> btnSave_click());
    btnLoad.addActionListener(q -> btnLoad_click());

    JPanel ret = LayoutManager.createBorderedPanel(
        null,
        null,
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 4,
            btnLoad,
            btnSave),
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 4,
            btnCancel,
            btnApply),
        null);

    ret = LayoutManager.createBorderedPanel(4, ret);

    return ret;
  }

  private void btnLoad_click() {
    JFileChooser jfc = SwingFactory.createFileDialog(SwingFactory.FileDialogType.startupSettings, lastStartupSettingsFileName);

    int res = jfc.showOpenDialog(this);
    if (res != JFileChooser.APPROVE_OPTION) return;

    File file = jfc.getSelectedFile();

    StartupSettings sett;
    sett = XmlLoadHelper.loadStartupSettings(file.getAbsolutePath());
    this.fillBySettings(sett);
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
    this.lastStartupSettingsFileName = fileName;
  }

  private JPanel createContentPanel() {
    JPanel ret = new JPanel();

    JTabbedPane tabbedPane = new JTabbedPane();

    AirportAndWeatherPanel pnlA = new AirportAndWeatherPanel();
    tabbedPane.addTab("Airport & Weather", pnlA);

    TrafficPanel pnlB = new TrafficPanel();
    pnlA.getOnAirportChanged().add(q->{pnlB.airportChanged((Airport) q);});
    tabbedPane.addTab("Traffic", pnlB);

    SimulationTimeRadarSettings pnlC = new SimulationTimeRadarSettings();
    tabbedPane.addTab("Simulation", pnlC);

    ret.add(tabbedPane);

    return ret;
  }

  private JPanel createTopPanel() {
    JPanel ret = new FilesPanel();
    return ret;
  }
}
