package eng.jAtcSim.startup;

import eng.eSystem.utilites.ExceptionUtil;
import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.startup.extenders.SwingFactory;
import eng.jAtcSim.startup.startupSettings.FrmStartupSettings;
import eng.jAtcSim.startup.startupSettings.StartupSettings;

import javax.swing.*;
import java.awt.*;

public class FrmIntro extends JFrame {

  private static final Dimension BUTTON_DIMENSION = new Dimension(100, 15);
  private StartupSettings startupSettings;

  public FrmIntro(StartupSettings startupSettings) {
    initializeComponents();
    this.setTitle("JAtcSim - Main menu");
    this.startupSettings = startupSettings;

  }

  public StartupSettings getStartupSettings() {
    return startupSettings;
  }

  private void initializeComponents() {
    JButton btnStartupSettings = new JButton("Adjust startupSettings settings");
    btnStartupSettings.addActionListener(o -> btnStartupSettings_click());
    JButton btnRun = new JButton("Start simulation");
    btnRun.addActionListener(o -> btnRun_click());
    JButton btnLoadSim = new JButton("Load simulation");
    btnLoadSim.addActionListener(q -> btnLoadSim_click());
    JButton btnExit = new JButton("Quit");
    btnExit.addActionListener(o -> btnExit_click());

    JPanel pnl = LayoutManager.createBorderedPanel(16);
    pnl.add(
        LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center, 16,
            btnStartupSettings, btnRun, btnLoadSim, btnExit));

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnl);
    this.pack();
    this.setLocationRelativeTo(null);
  }


  private void btnStartupSettings_click() {
    FrmStartupSettings frm = new FrmStartupSettings();
    Stylist.apply(frm, true);
    frm.fillBySettings(this.startupSettings);
    SwingFactory.showDialog(frm, "Startup settings", this);

    if (frm.isDialogResultOk()) {
      frm.fillSettingsBy(this.startupSettings);
    }

  }

  private void btnRun_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    try {
      JAtcSim.startSimulation(this.startupSettings);
    } catch (Exception ex) {
      MessageBox.show("Failed to start up the simulation. Something is wrong. Check the startupSettings settings. \n\n" +
          ExceptionUtil.toFullString(ex, "\n"), "Error during simulation start-up.");
      this.setVisible(true);
    }
  }

  private void btnLoadSim_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    JAtcSim.loadSimulation(this.startupSettings, "R:\\simSave.xml");
  }

  private void btnExit_click() {
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    this.dispose();
    JAtcSim.quit();
  }
}
