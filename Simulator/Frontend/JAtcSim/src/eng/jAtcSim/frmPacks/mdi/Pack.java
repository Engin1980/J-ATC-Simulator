/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.frmPacks.mdi;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;

/**
 * @author Marek
 */
public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private final EventSimple<eng.jAtcSim.frmPacks.Pack> em = new EventSimple<>(this);
  private IGame game;
  private ISimulation sim;
  private Area area;
  private RadarStyleSettings displaySettings;
  private FrmMain frmMain;
  private FrmFlightList frmList;
  private FrmScheduledTrafficListing frmScheduledTrafficListing;
  private AppSettings appSettings;

  public Pack() {
  }

  @Override
  public void initPack(IGame game, AppSettings appSettings) {

    String fileName = appSettings.radar.styleSettingsFile.toString();
    this.displaySettings = RadarStyleSettings.load(fileName); // XmlLoadHelper.loadNewDisplaySettings(fileName);
    this.appSettings  = appSettings;

    // init sim & area
    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();

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

  @Override
  public IMap<String, Object> getDataToStore() {
    return new EMap<>();
  }

  @Override
  public AppSettings getAppSettings() {
    return this.appSettings;
  }

  @Override
  public void applyStoredData(IMap<String, Object> map) {
// nothing to do
  }

  ISimulation getSim() {
    return sim;
  }

  Area getArea() {
    return area;
  }

  RadarStyleSettings getDisplaySettings() {
    return displaySettings;
  }

}
