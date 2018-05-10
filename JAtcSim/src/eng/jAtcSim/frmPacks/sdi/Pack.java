package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.Game;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.radarBase.DisplaySettings;

import javax.swing.*;
import java.nio.file.Paths;

public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private Game game;
  private Simulation sim;
  private Area area;
  private Airport aip;
  private AppSettings appSettings;
  private DisplaySettings displaySettings;
  private FrmMain frmMain;

  @Override
  public void initPack(Game game, AppSettings appSettings) {

    this.game = game;
    this.sim = game.getSimulation();
    this.area = game.getSimulation().getArea();
    this.aip = sim.getActiveAirport();
    this.appSettings = appSettings;

    String fileName = Paths.get(appSettings.resourcesFolder.toString() , "radarDisplaySettings.xml").toString();
    this.displaySettings = XmlLoadHelper.loadNewDisplaySettings(fileName);

    // create windows
    this.frmMain = new FrmMain();
    frmMain.init(this);
    this.frmMain.setVisible(true);
    this.frmMain.setExtendedState(this.frmMain.getExtendedState() | JFrame.MAXIMIZED_BOTH);
  }

  @Override
  public void startPack() {
    this.sim.start();
  }

  @Override
  public EventSimple<eng.jAtcSim.frmPacks.Pack> getElapseSecondEvent() {
    throw new UnsupportedOperationException();
  }

  public Simulation getSim() {
    return sim;
  }

  public Area getArea() {
    return area;
  }

  public Airport getAip() {
    return aip;
  }

  public DisplaySettings getDisplaySettings() {
    return displaySettings;
  }

  public AppSettings getAppSettings() {
    return appSettings;
  }
}
