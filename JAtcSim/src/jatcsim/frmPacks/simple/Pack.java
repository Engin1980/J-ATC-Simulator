/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks.simple;

import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.traffic.Movement;
import jatcsimlib.world.Area;

/**
 * @author Marek
 */
public class Pack extends jatcsim.frmPacks.Pack {

  private Simulation sim;
  private Area area;
  private Settings displaySettings;
  private FrmMain frmMain;
  private FrmFlightList frmList;
  private FrmScheduledTrafficListing frmScheduledTrafficListing;
  private int lastMovementCount = 0;

  private final EventManager<jatcsim.frmPacks.Pack, EventListener, Object> em = new EventManager<>(this);
  public EventManager<jatcsim.frmPacks.Pack, EventListener, Object> getElapseSecondEvent(){
    return em;
  }

  public Pack() {
  }

  Simulation getSim() {
    return sim;
  }

  Area getArea() {
    return area;
  }

  Settings getDisplaySettings() {
    return displaySettings;
  }

  @Override
  public void initPack(Simulation sim, Area area, Settings displaySettings) {
    this.sim = sim;
    this.area = area;
    this.displaySettings = displaySettings;
    this.sim.secondElapsed = new EventListener<Simulation, Object>() {

      @Override
      public void raise(Simulation parent, Object e) {
        frmMain.elapseSecond();
        frmList.elapseSecond();
        updateScheduledTrafficListing();
        em.raise(null);
      }
    };

    this.frmMain = new FrmMain();
    frmMain.init(this);

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
    // todo always creates an array as a copy of list of scheduled movements, what can be time consuming
    Movement[] movements = this.sim.getScheduledMovements();
    if (movements.length != lastMovementCount) {
      frmScheduledTrafficListing.refresh(movements);
      lastMovementCount = movements.length;
    }
  }
}
