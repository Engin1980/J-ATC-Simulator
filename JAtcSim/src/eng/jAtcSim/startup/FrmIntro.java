package eng.jAtcSim.startup;

import eng.eSystem.utilites.awt.ComponentUtils;

import javax.swing.*;
import java.awt.*;

public class FrmIntro extends JFrame {

  private StartupSettings startupSettings;
  private DialogResult dialogResult = DialogResult.cancel;

  private static final Dimension BUTTON_DIMENSION = new Dimension(100, 15);

  public FrmIntro(){
    initializeComponents();
  }

  public void setStartupSettings(StartupSettings startupSettings) {
    this.startupSettings = startupSettings;
  }

  public DialogResult getDialogResult() {
    return dialogResult;
  }

  public StartupSettings getStartupSettings() {
    return startupSettings;
  }

  private void initializeComponents() {
    JButton btnWizard = new JButton("Adjust startup settings");
    btnWizard.addActionListener(o->btnWizard_click());
    JButton btnRun = new JButton("Start simulation");
    btnRun.addActionListener(o->btnRun_click());
    JButton btnSave = new JButton("Save startup settings");
    btnSave.setEnabled(false);
    JButton btnLoad = new JButton("Load startup settings");
    btnLoad.setEnabled(false);
    JButton btnExit = new JButton("Quit");
    btnExit.addActionListener(o->btnExit_click());

    JPanel pnl = LayoutManager.createBorderedPanel(16);
    pnl.add(
        LayoutManager.createBoxPanel(LayoutManager.eHorizontalAlign.center , 16,
            btnWizard, btnRun, btnSave, btnLoad, btnExit));

    //ComponentUtils.adjustComponentTree(pnl, o->o instanceof  JButton, o-> { o.setPreferredSize(BUTTON_DIMENSION); o.setMinimumSize(BUTTON_DIMENSION);});

    // this.setPreferredSize(new Dimension(300, 500));
    this.getContentPane().setLayout(new BorderLayout());
    this.getContentPane().add(pnl);
    this.pack();
  }

  private void btnWizard_click(){
    ComponentUtils.adjustComponentTree(this, o -> o.setEnabled(false));
    StartupWizard wizard = new StartupWizard(startupSettings);
    wizard.run();
    ComponentUtils.adjustComponentTree(this, o -> o.setEnabled(true));
  }

  private void btnRun_click(){
    this.dialogResult = DialogResult.ok;
    this.setVisible(false);
  }

  private void btnExit_click(){
    this.dialogResult = DialogResult.cancel;
    this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    this.setVisible(false);
  }
}
