/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.startup.NewStartupSettings;
import eng.jAtcSim.startup.StartupWizard;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.traffic.CustomTraffic;
import jatcsimlib.traffic.TestTrafficOneApproach;
import jatcsimlib.traffic.Traffic;
import jatcsimlib.weathers.Weather;
import jatcsimlib.weathers.WeatherProvider;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;

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
  //new TestTrafficOneDeparture();
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
    NewStartupSettings sett = XmlLoadHelper.loadStartupSettings(file);
    StartupWizard wizard = new StartupWizard(sett);
    if (FAST_START == false) {
      wizard.run();
      if (wizard.isFinished() == false) {
        return;
      }
    }

    XmlLoadHelper.saveStartupSettings(sett, file);

    // loading data from Xml files
    try {
      loadDataFromXmlFiles(sett);
    } catch (Exception ex) {
      throw (ex);
    }

    area.initAfterLoad();

    System.out.println("** Setting simulation");

    // area, airport and time
    String icao = sett.recent.icao;
    Calendar simTime = Calendar.getInstance();
    updateCalendarToSimTime(simTime, sett);
    Airport aip = area.getAirports().get(icao);

    // weather
    Weather weather;
    if (sett.weather.useOnline) {
      weather = WeatherProvider.downloadAndDecodeMetar(aip.getIcao());
    } else {
      weather = WeatherProvider.decodeMetar(sett.weather.metar);
    }

    // traffic
    Traffic traffic = getTrafficFromStartupSettings(sett);
    if (specificTraffic != null)
      traffic = specificTraffic;
    final Simulation sim = Simulation.create(
        aip, types, weather, traffic, simTime, sett.simulation.secondLengthInMs);

    // sound
    SoundManager.init(appSettings.soundFolder);

    // starting pack & simulation
    String packType = sett.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(sim, area, appSettings);
    simPack.startPack();
  }




  private static void loadDataFromXmlFiles(NewStartupSettings sett) throws Exception {
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

  private static void updateCalendarToSimTime(Calendar simTime, NewStartupSettings sett) {
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
      throw new RuntimeException("Failed to create instance of radar pack " + packTypeName + ". Reason: " + ex.getMessage(), ex);
    }
    Pack ret = (Pack) object;
    return ret;
  }

  private static Traffic getTrafficFromStartupSettings(NewStartupSettings sett) {
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

