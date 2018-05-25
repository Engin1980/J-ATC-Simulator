/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.startup.startupWizard;

import eng.jAtcSim.Stylist;
import eng.jAtcSim.startup.*;

/**
 * @author Marek Vajgl
 */
public class StartupWizard {

  private boolean finished = false;
  private StartupSettings settings = null;

  public StartupWizard(StartupSettings settings) {
    this.settings = settings;
    this.buildPanels();
  }

  public void run() {
    this.navigateToPage(0);

  }

  public boolean isFinished() {
    return this.finished;
  }

  private JWizardPanel [] panels;

  private void buildPanels(){
    panels = new JWizardPanel[4];
    panels[0] = new PnlWizardAreaAndPlaneTypes();
    panels[1] = new PnlWizardAirportTimeAndWeather();
    panels[2] = new PnlWizardTraffic();
    panels[3] = new PnlWizardSimulationAndRadar();

//    for (JWizardPanel panel : panels) {
//      Stylist.apply(panel, true);
//    }
  }

  public void navigateToPage(int newPageIndex) {
    navigateToPage(-1, newPageIndex);
  }

  public void navigateToPage(int currentPageIndex, int newPageIndex) {
    if (currentPageIndex >= 0){
      panels[currentPageIndex].fillSettingsBy(settings);
    }

    if (newPageIndex < 0 || newPageIndex >= panels.length) {
      endWizard();
    } else {
      JWizardPanel pnl = panels[newPageIndex];
      pnl.fillBySettings(settings);
      FrmWizardFrameNew frm = new FrmWizardFrameNew(
          this, newPageIndex, pnl
      );
      Stylist.apply(frm, true);
      frm.setVisible(true);
    }
  }

  private void endWizard() {
    FrmIntro frm = new FrmIntro(settings);
    frm.setVisible(true);
  }
}
