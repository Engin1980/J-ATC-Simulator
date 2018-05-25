package eng.jAtcSim.startup;

import eng.jAtcSim.JAtcSim;
import eng.jAtcSim.Stylist;
import eng.jAtcSim.startup.startupWizard.FrmTest;
import eng.jAtcSim.startup.startupWizard.StartupWizard;

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
    JButton btnTest = new JButton("test");
    btnTest.addActionListener(q->btnTest_click());

    JButton btnWizard = new JButton("Adjust startup settings");
    btnWizard.addActionListener(o->btnWizard_click());
    JButton btnRun = new JButton("Start simulation");
    btnRun.addActionListener(o->btnRun_click());
    JButton btnSave = new JButton("Save startup settings");
    btnSave.setEnabled(false);
    JButton btnLoad = new JButton("Load startup settings");
    btnLoad.setEnabled(false);
    JButton btnLoadSim = new JButton("Load simulation");
    btnLoadSim.addActionListener(q->btnLoadSim_click());
    JButton btnExit = new JButton("Quit");
    btnExit.addActionListener(o->btnExit_click());

    JPanel pnl = LayoutManager.createBorderedPanel(16);
    pnl.add(
        LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center , 16,
            btnTest, btnWizard, btnRun, btnSave, btnLoad, btnLoadSim, btnExit));

    //ComponentUtils.adjustComponentTree(pnl, o->o instanceof  JButton, o-> { o.setPreferredSize(BUTTON_DIMENSION); o.setMinimumSize(BUTTON_DIMENSION);});

    // this.setPreferredSize(new Dimension(300, 500));
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnl);
    this.pack();
  }

  private void btnTest_click() {
    FrmTest frm = new FrmTest();
    Stylist.apply(frm, true);
    frm.pack();
    frm.setVisible(true);
  }

  private void btnWizard_click(){
    StartupWizard wizard = new StartupWizard(startupSettings);
    wizard.run();
    this.setVisible(false);
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
