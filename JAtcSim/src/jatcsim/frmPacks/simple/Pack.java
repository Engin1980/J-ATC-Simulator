/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsim.frmPacks.simple;

import JAtcSim.radarBase.DisplaySettings;
import JAtcSim.radarBase.parsing.RadarColorParser;
import JAtcSim.radarBase.parsing.RadarFontParser;
import eng.eSystem.xmlSerialization.XmlSerializer;
import jatcsim.AppSettings;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.events.EventListener;
import jatcsimlib.events.EventManager;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.traffic.Movement;
import jatcsimlib.world.Area;

/**
 * @author Marek
 */
public class Pack extends jatcsim.frmPacks.Pack {

  private final EventManager<jatcsim.frmPacks.Pack, EventListener, Object> em = new EventManager<>(this);
  private Simulation sim;
  private Area area;
  private DisplaySettings displaySettings;
  private FrmMain frmMain;
  private FrmFlightList frmList;
  private FrmScheduledTrafficListing frmScheduledTrafficListing;
  private int lastMovementCount = 0;

  public Pack() {
  }

  @Override
  public void initPack(Simulation sim, Area area, AppSettings appSettings) {

    String fileName = appSettings.resFolder + "radarDisplaySettings.xml";
    this.displaySettings = jatcsim.XmlLoadHelper.loadNewDisplaySettings(fileName);

    // init sim & area
    this.sim = sim;
    this.area = area;

    // create windows
    this.frmMain = new FrmMain();
    frmMain.init(this);

    this.frmList = new FrmFlightList();
    frmList.init(sim);

    this.frmScheduledTrafficListing = new FrmScheduledTrafficListing();

    // adjust window layout
    this.frmList.setSize(this.frmList.getSize().width, frmMain.getSize().height);
    this.frmMain.setLocation((this.frmList.getSize().width), this.frmMain.getLocation().y);

    // show windows
    this.frmList.setVisible(true);
    this.frmMain.setVisible(true);
    this.frmScheduledTrafficListing.setVisible(true);

    // TODO update this
//    // join listeners on second elapsed
//    this.sim.secondElapsedEvent = new EventListener<Simulation, Object>() {
//      @Override
//      public void raise(Simulation parent, Object e) {
//        frmMain.elapseSecond();
//        frmList.elapseSecond();
//        updateScheduledTrafficListing();
//        em.raise(null);
//      }
//    };
  }

  @Override
  public void startPack() {
    this.sim.start();
  }

  public EventManager<jatcsim.frmPacks.Pack, EventListener, Object> getElapseSecondEvent() {
    return em;
  }

  Simulation getSim() {
    return sim;
  }

  Area getArea() {
    return area;
  }

  DisplaySettings getDisplaySettings() {
    return displaySettings;
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
