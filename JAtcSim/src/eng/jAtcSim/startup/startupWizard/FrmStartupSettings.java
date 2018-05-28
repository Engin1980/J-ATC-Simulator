package eng.jAtcSim.startup.startupWizard;

import eng.eSystem.utilites.awt.ComponentUtils;
import eng.jAtcSim.startup.LayoutManager;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.startupWizard.panels.*;

import javax.swing.*;
import java.awt.*;

public class FrmStartupSettings extends JFrame {
  private JPanel pnlContent;

  public FrmStartupSettings() throws HeadlessException {

    // top
    JPanel pnlTop = createTopPanel();

    // content
    pnlContent = createContentPanel();

    // bottom
    JPanel pnlBottom = createBottomPanel();

    JPanel pnl = LayoutManager.createBorderedPanel(pnlTop, pnlBottom, null, null, pnlContent);

    this.setContentPane(pnl);
    this.pack();
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
    JPanel ret = LayoutManager.createBorderedPanel(
        null,
        null,
        LayoutManager.createFlowPanel(LayoutManager.eVerticalAlign.bottom, 4,
            new JButton("Save"),
            new JButton("Load")),
        new JButton("Apply"),
        null);

    ret = LayoutManager.createBorderedPanel(4, ret);

    return ret;
  }

  private JPanel createContentPanel() {
    JPanel ret = new JPanel();

    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel pnl;

    pnl = new AirportAndWeatherPanel();
    tabbedPane.addTab("Airport & Weather", pnl);

    pnl = new TrafficPanel();
    tabbedPane.addTab("Traffic", pnl);

    pnl = new SimulationTimeRadarSettings();
    tabbedPane.addTab("Simulation", pnl);

    ret.add(tabbedPane);

    return ret;
  }

  private JPanel createTopPanel() {
    JPanel ret = new FilesPanel();
    return ret;
  }
}
