/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.frmPacks.mdi;

import eng.jAtcSim.radarBase.DisplaySettings;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.world.Area;

/**
 * @author Marek
 */
public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private final EventSimple<eng.jAtcSim.frmPacks.Pack> em = new EventSimple<>(this);
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

    String fileName = appSettings.resourcesFolder + "radarDisplaySettings.xml";
    this.displaySettings = XmlLoadHelper.loadNewDisplaySettings(fileName);

    // init sim & area
    this.sim = sim;
    this.area = area;

    // create windows
    this.frmMain = new FrmMain();
    frmMain.init(this);

    this.frmList = new FrmFlightList();
    frmList.init(sim, appSettings);

    this.frmScheduledTrafficListing = new FrmScheduledTrafficListing();
    this.frmScheduledTrafficListing.init(sim, appSettings);

    // adjust window layout
    this.frmList.setSize(this.frmList.getSize().width, frmMain.getSize().height);
    this.frmMain.setLocation((this.frmList.getSize().width), this.frmMain.getLocation().y);

    // show windows
    this.frmList.setVisible(true);
    this.frmMain.setVisible(true);
    this.frmScheduledTrafficListing.setVisible(true);
  }

  @Override
  public void startPack() {
    this.sim.start();
  }

  public EventSimple<eng.jAtcSim.frmPacks.Pack> getElapseSecondEvent() {
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

}
