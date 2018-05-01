/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
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
import eng.jAtcSim.startup.StartupSettings;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;

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

    Acc.setLog(new ApplicationLog());

    appSettings = new AppSettings();

    Recorder.init(appSettings.logFolder.toString());

    // startup wizard
    StartupSettings startupSettings = XmlLoadHelper.loadStartupSettings(appSettings.getStartupSettingsFile());

    FrmIntro frmIntro = new FrmIntro(startupSettings);
    frmIntro.setVisible(true);
  }

  public static void loadSimulation(StartupSettings startupSettings, String xmlFileName){
    System.out.println("* Loading simulation");
    Simulation sim = Simulation.load(xmlFileName);

    System.out.println("* Initialization of the simulation");
    sim.init();

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.soundFolder.toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(sim, sim.getArea(), appSettings);
    simPack.startPack();
  }

  public static void startSimulation(StartupSettings startupSettings) {

    KeyList.setDuplicatesChecking(false);

    resolveShortXmlFileNamesInStartupSettings(appSettings, startupSettings);
    XmlLoadHelper.saveStartupSettings(startupSettings, appSettings.getStartupSettingsFile());

    System.out.println("* Loading area");
    AreaXmlSource areaXmlSource = new AreaXmlSource(startupSettings.files.areaXmlFile);
    areaXmlSource.load();
    areaXmlSource.init(startupSettings.recent.icao);

    System.out.println("* Loading plane types");
    AirplaneTypesXmlSource airplaneTypesXmlSource = new AirplaneTypesXmlSource(startupSettings.files.planesXmlFile);
    airplaneTypesXmlSource.load();
    airplaneTypesXmlSource.init();

    System.out.println("* Loading fleets");
    FleetsXmlSource fleetsXmlSource = new FleetsXmlSource(startupSettings.files.fleetsXmlFile);
    fleetsXmlSource.load();
    fleetsXmlSource.init(airplaneTypesXmlSource.getContent());

    System.out.println("* Loading traffic");
    TrafficXmlSource trafficXmlSource = new TrafficXmlSource(startupSettings.files.trafficXmlFile);
    trafficXmlSource.load();
    trafficXmlSource.init(areaXmlSource.getActiveAirport(), specificTraffic);

    System.out.println("* Initializing weather");
    WeatherProvider weatherProvider;
    if (startupSettings.weather.useOnline) {
      DynamicWeatherProvider dwp = new NoaaDynamicWeatherProvider(areaXmlSource.getActiveAirport().getIcao());
      dwp.updateWeather(false);
      weatherProvider = dwp;
    } else {
      StaticWeatherProvider swp = new StaticWeatherProvider(startupSettings.weather.metar);
      weatherProvider = swp;
    }

    System.out.println("* Preparing the simulation");
    Calendar simTime = Calendar.getInstance();
    updateCalendarToSimTime(simTime, startupSettings);
    if (specificTraffic != null)
      trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.specificTraffic, 0 );
     else
       trafficXmlSource.setActiveTraffic(TrafficXmlSource.TrafficSource.activeAirportTraffic, 0 );

    // enable duplicates
    KeyList.setDuplicatesChecking(true);

    System.out.println("* Creating the simulation");

    // simulation creation
    final Simulation sim = new Simulation(
        areaXmlSource, airplaneTypesXmlSource, fleetsXmlSource, trafficXmlSource,
        weatherProvider, simTime,
        startupSettings.simulation.secondLengthInMs,
        startupSettings.simulation.emergencyPerDayProbability);
    sim.init();

    System.out.println("* Initializing sound environment");
    // sound
    SoundManager.init(appSettings.soundFolder.toString());

    System.out.println("* Starting a GUI");
    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(sim, sim.getArea(), appSettings);
    simPack.startPack();
  }

  public static void quit() {

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

    tmp = Paths.get(startupSettings.files.trafficXmlFile);
    if (tmp.isAbsolute()) {
      tmp = appPath.relativize(tmp);
      startupSettings.files.trafficXmlFile = tmp.toString();
    }
  }

  private static void updateCalendarToSimTime(Calendar simTime, StartupSettings sett) {
    String timeS = sett.recent.time;
    String[] pts = timeS.split(":");
    int hours = Integer.parseInt(pts[0]);
    int minutes = Integer.parseInt(pts[1]);
    simTime.set(Calendar.HOUR_OF_DAY, hours);
    simTime.set(Calendar.MINUTE, minutes);
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

