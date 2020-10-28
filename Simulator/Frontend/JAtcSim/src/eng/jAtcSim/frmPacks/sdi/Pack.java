package eng.jAtcSim.frmPacks.sdi;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IMap;
import eng.eSystem.events.EventSimple;
import eng.eSystem.exceptions.EApplicationException;
import eng.eXmlSerialization.XmlSerializer;
import eng.jAtcSim.AppSettings;
import eng.jAtcSim.XmlLoadHelper;
import eng.jAtcSim.abstractRadar.RadarViewPort;
import eng.jAtcSim.abstractRadar.settings.RadarStyleSettings;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.gameSim.IGame;
import eng.jAtcSim.newLib.gameSim.ISimulation;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.DynamicPlaneFormatter;
import eng.jAtcSim.newLib.textProcessing.implemented.dynamicPlaneFormatter.types.Sentence;
import eng.jAtcSim.xmlLoading.XmlSerialization;
import eng.jAtcSim.xmlLoading.XmlSerializationFactory;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class Pack extends eng.jAtcSim.frmPacks.Pack {

  private IGame game;
  private ISimulation sim;
  private Area area;
  private Airport aip;
  private AppSettings appSettings;
  private RadarStyleSettings radarStyleSettings;
  private DynamicPlaneFormatter dynamicPlaneFormatter;
  private FrmMain frmMain;

  @Override
  public void applyStoredData(IMap<String, Object> map) {
    IMap<Integer, RadarViewPort> storedRadarPositions = (IMap<Integer, RadarViewPort>) map.tryGet("StoredRadarPositions");
    frmMain.setRadarStoredPositions(storedRadarPositions);
  }

  public Airport getAip() {
    return aip;
  }

  public AppSettings getAppSettings() {
    return appSettings;
  }

  public Area getArea() {
    return area;
  }

  @Override
  public IMap<String, Object> getDataToStore() {
    IMap<String, Object> ret = new EMap<>();
    ret.set("StoredRadarPositions", frmMain.getRadarStoredPositions());
    return ret;
  }

  @Override
  public DynamicPlaneFormatter getDynamicPlaneFormatter() {
    return dynamicPlaneFormatter;
  }

  @Override
  public EventSimple<eng.jAtcSim.frmPacks.Pack> getElapseSecondEvent() {
    throw new UnsupportedOperationException();
  }

  public IGame getGame() {
    return game;
  }

  public RadarStyleSettings getRadarStyleSettings() {
    return radarStyleSettings;
  }

  public ISimulation getSim() {
    return sim;
  }

  @Override
  public void initPack(IGame game, AppSettings appSettings) {

    this.game = game;
    this.sim = game.getSimulation();
    if (appSettings.autosave.intervalInSeconds > 0) {
      this.sim.registerOnSecondElapsed(this::sim_secondElapsed);
      if (java.nio.file.Files.exists(appSettings.autosave.path) == false) {
        try {
          java.nio.file.Files.createDirectories(appSettings.autosave.path);
        } catch (IOException e) {
          throw new EApplicationException("Unable to create directory " + appSettings.autosave.path + " specified in appSettings for autosaves.");
        }
      }
    }
    this.area = game.getSimulation().getArea();
    this.aip = sim.getAirport();
    this.appSettings = appSettings;

    String fileName = appSettings.radar.styleSettingsFile.toString();
    this.radarStyleSettings = XmlLoadHelper.loadNewDisplaySettings(fileName);

    this.dynamicPlaneFormatter = loadDynamicPlaneFormatter(appSettings.speechFormatterFile);

    // create windows
    this.frmMain = new FrmMain();
    frmMain.init(this);
    this.frmMain.setVisible(true);
    this.frmMain.setExtendedState(this.frmMain.getExtendedState() | JFrame.MAXIMIZED_BOTH);

  }

  private DynamicPlaneFormatter loadDynamicPlaneFormatter(Path speechFormatterFile) {
    IMap<Class<?>, IList<Sentence>> speechResponses;
    try {
      XmlSerializer ser = XmlSerializationFactory.createForSpeechResponses();
      speechResponses = XmlSerialization.loadFromFile(ser, speechFormatterFile.toFile(), IMap.class);
    } catch (EApplicationException ex) {
      throw new EApplicationException(
              sf("Unable to load speech responses from xml file '%s'.", speechFormatterFile), ex);
    }
    DynamicPlaneFormatter ret = new DynamicPlaneFormatter(speechResponses);
    return ret;
  }

  @Override
  public void startPack() {
    this.sim.start();
  }

  private void sim_secondElapsed(ISimulation simulation) {
    EDayTimeStamp now = simulation.getNow();
    int secs = now.getValue();
    if (secs % appSettings.autosave.intervalInSeconds == 0) {
      // doing autosave
      String fileName = simulation.getAirport().getIcao() + "_" +
              String.format("%d_%02d_%02d_%02d", now.getDays(), now.getHours(), now.getMinutes(), now.getSeconds()) +
              ".autosave.sm.xml";

      Path p = Paths.get(appSettings.autosave.path.toAbsolutePath().toString(), fileName);

      IMap<String, Object> tmp = this.getDataToStore();
      IMap<String, String> todo = new EMap<>();
      this.getGame().save(p.toAbsolutePath().toString(), todo);
    }
  }
}
