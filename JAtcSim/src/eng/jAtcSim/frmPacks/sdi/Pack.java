package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.events.EventSimple;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.radarBase.DisplaySettings;

import javax.swing.*;

public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private Simulation sim;
  private Area area;
  private Airport aip;
  private AppSettings sett;
  private DisplaySettings displaySettings;
  private FrmMain frmMain;

  @Override
  public void initPack(Simulation sim, Area area, AppSettings appSettings) {

    this.sim = sim;
    this.area = area;
    this.aip = sim.getActiveAirport();
    this.sett = appSettings;

    String fileName = appSettings.resFolder + "radarDisplaySettings.xml";
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
}
