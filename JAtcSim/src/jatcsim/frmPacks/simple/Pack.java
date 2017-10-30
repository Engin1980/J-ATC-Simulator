/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks.simple;

import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.events.EventListener;
import jatcsimlib.traffic.Movement;
import jatcsimlib.world.Area;

/**
 * @author Marek
 */
public class Pack extends jatcsim.frmPacks.Pack {

  private Simulation sim;
  private FrmMain frmMain;
  private FrmFlightList frmList;
  private FrmScheduledTrafficListing frmScheduledTrafficListing;
  private int lastMovementCount = 0;

  public Pack() {
  }

  @Override
  public void initPack(Simulation sim, Area area, Settings displaySettings) {
    this.sim = sim;
    this.sim.secondElapsed = new EventListener<Simulation, Object>() {

      @Override
      public void raise(Simulation parent, Object e) {
        frmMain.elapseSecond();
        frmList.elapseSecond();
        updateScheduledTrafficListing();
      }
    };

    this.frmMain = new FrmMain();
    frmMain.init(sim, area, displaySettings);

    this.frmList = new FrmFlightList();
    frmList.init(sim);
    this.frmList.setSize(this.frmList.getSize().width, frmMain.getSize().height);

    this.frmMain.setLocation((this.frmList.getSize().width), this.frmMain.getLocation().y);

    this.frmScheduledTrafficListing = new FrmScheduledTrafficListing();
    this.frmScheduledTrafficListing.setVisible(true);
  }

  @Override
  public void startPack() {
    this.sim.start();
  }

  private void updateScheduledTrafficListing() {
    // todo allways creates an array as a copy of list of scheduled movements, what can be time consuming
    Movement[] movements = this.sim.getScheduledMovements();
    if (movements.length != lastMovementCount) {
      frmScheduledTrafficListing.refresh(movements);
      lastMovementCount = movements.length;
    }
  }
}
