/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.jAtcSim.lib.traffic.TestTrafficOneDeparture;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.StartupWizard;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.traffic.CustomTraffic;
import eng.jAtcSim.lib.traffic.TestTrafficOneApproach;
import eng.jAtcSim.lib.traffic.Traffic;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

/**
 * @author Marek
 */
public class JAtcSim {

  private static final boolean FAST_START = true;
  private static final Traffic specificTraffic =
      new TestTrafficOneApproach();
//  new TestTrafficOneDeparture();
  //null;

  private static Area area = null;
  private static AppSettings appSettings = new AppSettings();
  private static AirplaneTypes types = null;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    initResourcesFolder();

    // startup wizard
    String file = appSettings.resFolder + "startupSettings.xml";
    StartupSettings startupSettings = XmlLoadHelper.loadStartupSettings(file);
    StartupWizard wizard = new StartupWizard(startupSettings);
    if (FAST_START == false) {
      wizard.run();
      if (wizard.isFinished() == false) {
        return;
      }
    }

    XmlLoadHelper.saveStartupSettings(startupSettings, file);

    // loading data from Xml files
    try {
      loadDataFromXmlFiles(startupSettings);
    } catch (Exception ex) {
      throw (ex);
    }

    area.initAfterLoad();

    System.out.println("** Setting simulation");

    // area, airport and time
    String icao = startupSettings.recent.icao;
    Calendar simTime = Calendar.getInstance();
    updateCalendarToSimTime(simTime, startupSettings);
    Airport aip = area.getAirports().get(icao);

    // weather
    Weather weather;
    if (startupSettings.weather.useOnline) {
      weather = WeatherProvider.downloadAndDecodeMetar(aip.getIcao());
    } else {
      weather = WeatherProvider.decodeMetar(startupSettings.weather.metar);
    }

    // traffic
    Traffic traffic = getTrafficFromStartupSettings(startupSettings);
    if (specificTraffic != null)
      traffic = specificTraffic;
    final Simulation sim = Simulation.create(
        aip, types, weather, traffic, simTime, startupSettings.simulation.secondLengthInMs);

    // sound
    SoundManager.init(appSettings.soundFolder);

    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(sim, area, appSettings);
    simPack.startPack();
  }

  private static void loadDataFromXmlFiles(StartupSettings sett) throws Exception {
    System.out.println("*** Loading XML");

    String failMsg = null;
    String fileName = null;

    try {

//      fileName =
//          resFolder.toString() + "\\settings\\radarDisplaySettings.xml";
//          //"C:\\Users\\Marek Vajgl\\Documents\\IdeaProjects\\JAtcSimSolution\\JAtcSim\\resources\\settings\\radarDisplaySettings.xml";
//      failMsg = "Failed to load radar display settings from " + fileName;
//      JAtcSim.displaySettings = XmlLoadHelper.loadNewDisplaySettings(fileName);

      fileName = sett.files.areaXmlFile;
      failMsg = "Failed to load area from " + fileName;
      JAtcSim.area = XmlLoadHelper.loadNewArea(fileName);

      fileName = sett.files.planesXmlFile;
      failMsg = "Failed to load plane types from " + fileName;
      JAtcSim.types = XmlLoadHelper.loadPlaneTypes(fileName);

    } catch (Exception ex) {
      throw new ERuntimeException("Error reading XML file " + fileName + ". " + failMsg, ex);
    }
  }

  private static void initResourcesFolder() {
    String curDir = System.getProperty("user.dir") + "\\";
    appSettings.resFolder = curDir + "\\_SettingFiles\\";
    appSettings.soundFolder =curDir + "\\_Sounds\\";
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
          ex,
          "Failed to create instance of radar pack '%s'.", packTypeName);
    }
    Pack ret = (Pack) object;
    return ret;
  }

  private static Traffic getTrafficFromStartupSettings(StartupSettings sett) {
    Traffic ret;
    if (sett.traffic.useXml) {
      throw new UnsupportedOperationException("Traffic from XML files not supported yet.");
    } else {
      ret = new CustomTraffic(
          sett.traffic.movementsPerHour,
          1 - sett.traffic.arrivals2departuresRatio / 10d, // 0-10 to 0.0-1.0
          sett.traffic.maxPlanes,
          sett.traffic.vfr2ifrRatio / 10d, // dtto
          sett.traffic.weightTypeA,
          sett.traffic.weightTypeB,
          sett.traffic.weightTypeC,
          sett.traffic.weightTypeD,
          sett.traffic.useExtendedCallsigns
      );
    }

    return ret;
  }
}

