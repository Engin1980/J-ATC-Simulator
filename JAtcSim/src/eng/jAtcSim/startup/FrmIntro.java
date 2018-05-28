package eng.jAtcSim.startup;

import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.startup.startupWizard.FrmStartupSettings;

import javax.swing.*;
import java.awt.*;

public class FrmIntro extends JFrame {

  private StartupSettings startupSettings;
  private static final Dimension BUTTON_DIMENSION = new Dimension(100, 15);

  public FrmIntro(StartupSettings startupSettings){
    initializeComponents();
    this.startupSettings = startupSettings;

  }

  public StartupSettings getStartupSettings() {
    return startupSettings;
  }

  private void initializeComponents() {
    JButton btnStartupSettings = new JButton("Adjust startup settings");
    btnStartupSettings.addActionListener(o-> btnStartupSettings_click());
    JButton btnRun = new JButton("Start simulation");
    btnRun.addActionListener(o->btnRun_click());
    JButton btnLoadSim = new JButton("Load simulation");
    btnLoadSim.addActionListener(q->btnLoadSim_click());
    JButton btnExit = new JButton("Quit");
    btnExit.addActionListener(o->btnExit_click());

    JPanel pnl = LayoutManager.createBorderedPanel(16);
    pnl.add(
        LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center , 16,
            btnStartupSettings, btnRun, btnLoadSim, btnExit));

    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnl);
    this.pack();
  }


  private void btnStartupSettings_click(){
    FrmStartupSettings frm = new FrmStartupSettings();
    Stylist.apply(frm, true);
    frm.pack();
    frm.fillBySettings(this.startupSettings);
    frm.setVisible(true);
  }

  private void btnRun_click(){
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    JAtcSim.startSimulation(this.startupSettings);
  }

  private void btnLoadSim_click(){
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    JAtcSim.loadSimulation(this.startupSettings,"R:\\simSave.xml");
  }

  private void btnExit_click(){
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
    this.dispose();
    JAtcSim.quit();
  }
}
