/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim.startup;

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

    FrmWizardFrame.DialogResult res;

    if (settings == null) {
      settings = new StartupSettings();
    }

    res = doStep1();
    if (res != FrmWizardFrame.DialogResult.Ok) {
      return;
    }

    res = doStep2();
    if (res != FrmWizardFrame.DialogResult.Ok) {
      return;
    }

    res = doStep3();
    if (res != FrmWizardFrame.DialogResult.Ok) {
      return;
    }

    this.finished = true;
  }

  public boolean isFinished() {
    return this.finished;
  }

  private FrmWizardFrame.DialogResult doStep1() {
    FrmWizardFrame frm = new FrmWizardAreaAndPlaneTypes();
    frm.initSettings(settings);
    frm.showDialog();
    return frm.getDialogResult();
  }

  private FrmWizardFrame.DialogResult doStep2() {
    FrmWizardFrame frm = new FrmWizardAirportTimeAndWeather();
    frm.initSettings(settings);
    frm.showDialog();
    return frm.getDialogResult();
  }

  private FrmWizardFrame.DialogResult doStep3() {
    FrmWizardFrame frm = new FrmWizardTraffic();
    frm.initSettings(settings);
    frm.showDialog();
    return frm.getDialogResult();
  }
}
