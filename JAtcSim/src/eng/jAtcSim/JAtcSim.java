/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Game;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.KeyList;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.logging.Recorder;
import eng.jAtcSim.lib.global.sources.*;
import eng.jAtcSim.lib.traffic.GenericTraffic;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.weathers.*;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.startup.FrmIntro;
import eng.jAtcSim.startup.startupSettings.StartupSettings;

import javax.swing.border.TitledBorder;
import java.awt.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

/**
 * @author Marek
 */
public class JAtcSim {

  static class XmlLoadedData {
    public Area area;
    public AirplaneTypes types;
    public Fleets fleets;
  }

  private static final boolean FAST_START = false;
  private static final Traffic specificTraffic =
      //  new eng.jAtcSim.lib.traffic.TestTrafficOneApproach();
      // new eng.jAtcSim.lib.traffic.TestTrafficOneDeparture();
      null;
  private static AppSettings appSettings;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    initStylist();

    Acc.setLog(new ApplicationLog());

    appSettings = new AppSettings();

    Recorder.init(appSettings.logFolder.toString());

    // startupSettings wizard
    StartupSettings startupSettings = XmlLoadHelper.loadStartupSettings(appSettings.getStartupSettingsFile());

    FrmIntro frmIntro = new FrmIntro(startupSettings);
    Stylist.apply(frmIntro, true);
    frmIntro.setVisible(true);
  }

  public static void loadSimulation(StartupSettings startupSettings, String xmlFileName) {
    KeyList.setDuplicatesChecking(false);

    System.out.println("* Loading the simulation");

    Game g = Game.load(xmlFileName);

    // enable duplicates
    KeyList.setDuplicatesChecking(true);

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.soundFolder.toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(g, appSettings);
    simPack.startPack();
  }

  public static void startSimulation(StartupSettings startupSettings) {

    KeyList.setDuplicatesChecking(false);

    try {
      resolveShortXmlFileNamesInStartupSettings(appSettings, startupSettings);
      XmlLoadHelper.saveStartupSettings(startupSettings, appSettings.getStartupSettingsFile());
    } catch (EApplicationException ex) {
      throw new EApplicationException("Failed to normalize or save default settings.", ex);
    }

    System.out.println("* Creating the simulation");

    Game.GameStartupInfo gsi = new Game.GameStartupInfo();
    gsi.areaXmlFile = startupSettings.files.areaXmlFile;
    gsi.emergencyPerDayProbability = startupSettings.simulation.emergencyPerDayProbability;
    gsi.fleetsXmlFile = startupSettings.files.fleetsXmlFile;
    gsi.icao = startupSettings.recent.icao;
    gsi.planesXmlFile = startupSettings.files.planesXmlFile;
    gsi.secondLengthInMs = startupSettings.simulation.secondLengthInMs;
    gsi.specificTraffic = specificTraffic;
    gsi.startTime = new ETime(startupSettings.recent.time);
    gsi.trafficXmlFile = startupSettings.files.trafficXmlFile;
    gsi.initialWeather = Weather.createClear();
    gsi.weatherProviderType = startupSettings.weather.useOnline ?
        WeatherSource.ProviderType.dynamicNovGoaaProvider : WeatherSource.ProviderType.staticProvider;

    Game g;
    g = Game.create(gsi);

    // enable duplicates
    try {
      KeyList.setDuplicatesChecking(true);
    } catch (Exception ex){
      throw new EApplicationException("Some element in source XML files is not unique. Some of the input XML files is not valid.", ex);
    }

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.soundFolder.toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(g, appSettings);
    simPack.startPack();
  }

  public static void quit() {

  }

  private static void initStylist() {
    // default theme
    Stylist.add(
        new Stylist.TypeFilter(java.awt.Component.class, true),
        q -> {
          //q.setBackground(new Color(50, 50, 50));
          //q.setForeground(new Color(255, 255, 255));
          Font fnt = new Font("Verdana", 0, 12);
          q.setFont(fnt);
        });

    Stylist.add(
        new Stylist.TypeFilter(javax.swing.JPanel.class, false)
        ,
        q -> {
          javax.swing.JPanel p = (javax.swing.JPanel) q;
          TitledBorder b = (TitledBorder) p.getBorder();
          //if (b != null) b.setTitleColor(new Color(222, 222, 255));
        });

    // intro page
    Stylist.add(
        new Stylist.TypeFilter(FrmIntro.class, false),
        q -> {
          Dimension d = new Dimension(500, 360);
          q.setMinimumSize(d);
          q.setPreferredSize(d);
        });

    Stylist.add(
        new Stylist.AndFilter(
            new Stylist.TypeFilter(javax.swing.JButton.class, false),
            new Stylist.ParentTypeFilter(FrmIntro.class, true)
        )
        ,
        q -> {
          Dimension d = new Dimension(450, 32);
          q.setPreferredSize(d);
          q.setMinimumSize(d);
          q.setMaximumSize(d);
        });


  }

  private static void resolveShortXmlFileNamesInStartupSettings(AppSettings appSettings, StartupSettings startupSettings) {
    Path tmp;
    Path appPath;
    appPath = appSettings.applicationFolder;

    tmp = Paths.get(startupSettings.files.areaXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.areaXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.fleetsXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.fleetsXmlFile = tmp.toString();
    }

    tmp = Paths.get(startupSettings.files.planesXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.planesXmlFile = tmp.toString();
    }

    if (startupSettings.files.trafficXmlFile != null) {
      tmp = Paths.get(startupSettings.files.trafficXmlFile);
      if (tmp.isAbsolute()) {
        tmp = appPath.relativize(tmp);
        startupSettings.files.trafficXmlFile = tmp.toString();
      }
    }
  }

  private static Pack createPackInstance(String packTypeName) {
    Class<?> clazz;
    Object object;
    try {
      clazz = Class.forName(packTypeName);
      Constructor<?> ctor = clazz.getConstructor();
      object = ctor.newInstance();
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException ex) {
      throw new ERuntimeException(
          sf("Failed to create instance of radar pack '%s'.", packTypeName), ex);
    }
    Pack ret = (Pack) object;
    return ret;
  }

  private static Traffic getTrafficFromStartupSettings(StartupSettings sett) {
    Traffic ret;
    switch (sett.traffic.type) {
      case xml:
        ret = XmlLoadHelper.loadTraffic(sett.files.trafficXmlFile);
        break;
      case airportDefined:
        Area area = XmlLoadHelper.loadNewArea(sett.files.areaXmlFile);
        Airport airport = CollectionUtils.tryGetFirst(area.getAirports(), o -> o.getIcao().equals(sett.recent.icao));
        ret = CollectionUtils.tryGetFirst(airport.getTrafficDefinitions(), o -> o.getTitle().equals(sett.traffic.trafficAirportDefinedTitle));
        break;
      case custom:
        ret = new GenericTraffic(
            sett.traffic.customTraffic.movementsPerHour,
            1 - sett.traffic.customTraffic.arrivals2departuresRatio / 10d, // 0-10 to 0.0-1.0
            sett.traffic.customTraffic.weightTypeA,
            sett.traffic.customTraffic.weightTypeB,
            sett.traffic.customTraffic.weightTypeC,
            sett.traffic.customTraffic.weightTypeD,
            sett.traffic.customTraffic.useExtendedCallsigns
        );
      default:
        throw new UnsupportedOperationException();
    }

    return ret;
  }
}

