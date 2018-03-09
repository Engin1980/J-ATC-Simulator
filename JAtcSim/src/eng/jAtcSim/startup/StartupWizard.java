/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup;

import eng.jAtcSim.JAtcSim;

/**
 *
 * @author Marek Vajgl
 */
public class StartupWizard {

  private boolean finished = false;
  private StartupSettings settings = null;

  public StartupWizard() {
  }

  public StartupWizard(StartupSettings settings) {
    this.settings = settings;
  }

  public void run() {

    DialogResult res;

    if (settings == null) {
      settings = new StartupSettings();
    }

    res = doStep1();
    if (res != DialogResult.ok) {
      return;
    }

    res = doStep2();
    if (res != DialogResult.ok) {
      return;
    }

    res = doStep3();
    if (res != DialogResult.ok) {
      return;
    }
    
    res = doStep4();
    if (res != DialogResult.ok) {
      return;
    }

    this.finished = true;
  }

  public boolean isFinished() {
    return this.finished;
  }

  private DialogResult doStep1() {
    FrmWizardFrame frm = new FrmWizardAreaAndPlaneTypes();
    frm.initSettings(settings);
    frm.setVisible(true);
    JAtcSim.waitFor(frm, f->f.isVisible() == false);
    return frm.getDialogResult();
  }

  private DialogResult doStep2() {
    FrmWizardFrame frm = new FrmWizardAirportTimeAndWeather();
    frm.initSettings(settings);
    JAtcSim.waitFor(frm, f->f.isVisible() == false);
    return frm.getDialogResult();
  }

  private DialogResult doStep3() {
    FrmWizardFrame frm = new FrmWizardTraffic();
    frm.initSettings(settings);
    JAtcSim.waitFor(frm, f->f.isVisible() == false);
    return frm.getDialogResult();
  }
  
  private DialogResult doStep4() {
    FrmWizardFrame frm = new FrmWizardSimulationAndRadar();
    frm.initSettings(settings);
    JAtcSim.waitFor(frm, f->f.isVisible() == false);
    return frm.getDialogResult();
  }
}
