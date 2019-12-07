package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.newLib.Game;
import eng.jAtcSim.newLib.Simulation;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.Area;
import eng.jAtcSim.radarBase.RadarStyleSettings;
import eng.jAtcSim.radarBase.RadarViewPort;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private Game game;
  private Simulation sim;
  private Area area;
  private Airport aip;
  private AppSettings appSettings;
  private RadarStyleSettings radarStyleSettings;
  private FrmMain frmMain;

  public Game getGame() {
    return game;
  }

  @Override
  public void initPack(Game game, AppSettings appSettings) {

    this.game = game;
    this.sim = game.getSimulation();
    if (appSettings.autosave.intervalInSeconds > 0) {
      this.sim.getOnSecondElapsed().add(this::sim_secondElapsed);
      if (java.nio.file.Files.exists(appSettings.autosave.path) == false) {
        try {
          java.nio.file.Files.createDirectories(appSettings.autosave.path);
        } catch (IOException e) {
          throw new EApplicationException("Unable to create directory " + appSettings.autosave.path + " specified in appSettings for autosaves.");
        }
      }
    }
    this.area = game.getSimulation().getArea();
    this.aip = sim.getActiveAirport();
    this.appSettings = appSettings;

    String fileName = appSettings.radar.styleSettingsFile.toString();
    this.radarStyleSettings = XmlLoadHelper.loadNewDisplaySettings(fileName);

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

  @Override
  public IMap<String, Object> getDataToStore() {
    IMap<String, Object> ret = new EMap<>();
    ret.set("StoredRadarPositions", frmMain.getRadarStoredPositions());
    return ret;
  }

  public AppSettings getAppSettings() {
    return appSettings;
  }

  @Override
  public void applyStoredData(IMap<String, Object> map) {
    IMap<Integer, RadarViewPort> storedRadarPositions = (IMap<Integer, RadarViewPort>) map.tryGet("StoredRadarPositions");
    frmMain.setRadarStoredPositions(storedRadarPositions);
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

  public RadarStyleSettings getRadarStyleSettings() {
    return radarStyleSettings;
  }

  private void sim_secondElapsed(Simulation simulation) {
    ETime now = simulation.getNow();
    int secs = now.getTotalSeconds();
    if (secs % appSettings.autosave.intervalInSeconds == 0) {
      // doing autosave
      String fileName = simulation.getActiveAirport().getIcao() + "_" +
          String.format("%d_%02d_%02d_%02d", now.getDays(), now.getHours(), now.getMinutes(), now.getSeconds()) +
          ".autosave.sm.xml";

      Path p = Paths.get(appSettings.autosave.path.toAbsolutePath().toString(), fileName);

      IMap<String, Object> tmp = this.getDataToStore();
      this.getGame().save(p.toAbsolutePath().toString(), tmp);
    }
  }
}
