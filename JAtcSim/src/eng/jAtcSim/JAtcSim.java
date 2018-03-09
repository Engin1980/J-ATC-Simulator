/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim;

import eng.jAtcSim.lib.global.Recorder;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.radarBase.global.SoundManager;
import eng.jAtcSim.frmPacks.Pack;
import eng.jAtcSim.startup.StartupSettings;
import eng.jAtcSim.startup.StartupWizard;
import eng.jAtcSim.lib.Simulation;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.exceptions.ERuntimeException;
import eng.jAtcSim.lib.traffic.GenericTraffic;
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
//      new TestTrafficOneApproach();
//  new TestTrafficOneDeparture();
  null;

  private static AppSettings appSettings = new AppSettings();

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    initResourcesFolder();
    Recorder.setLogPathBase(appSettings.logFolder);

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

    XmlLoadedData data;

    // loading data from Xml files
    try {
      data = loadDataFromXmlFiles(startupSettings);
    } catch (Exception ex) {
      throw (ex);
    }

    data.area.initAfterLoad();

    System.out.println("** Setting simulation");

    // area, airport and time
    String icao = startupSettings.recent.icao;
    Calendar simTime = Calendar.getInstance();
    updateCalendarToSimTime(simTime, startupSettings);
    Airport aip = data.area.getAirports().get(icao);

    // weather
    Weather weather;
    if (startupSettings.weather.useOnline) {
      weather = WeatherProvider.downloadAndDecodeMetar(aip.getIcao());
    } else {
      weather = WeatherProvider.decodeMetar(startupSettings.weather.metar);
    }

    // traffic
    Traffic traffic = aip.getTrafficDefinitions().get(0); //getTrafficFromStartupSettings(startupSettings);
    if (specificTraffic != null)
      traffic = specificTraffic;

    // simulation creation
    final Simulation sim = Simulation.create(
        aip, data.types, weather, data.fleets, traffic, simTime, startupSettings.simulation.secondLengthInMs);

    // sound
    SoundManager.init(appSettings.soundFolder);

    // starting pack & simulation
    String packType = startupSettings.radar.packClass;
    Pack simPack
        = createPackInstance(packType);

    simPack.initPack(sim, data.area, appSettings);
    simPack.startPack();
  }

  private static XmlLoadedData loadDataFromXmlFiles(StartupSettings sett) throws Exception {
    System.out.println("*** Loading XML");

    XmlLoadedData ret = new XmlLoadedData();

    String failMsg = null;
    String fileName = null;

    try {
      fileName = sett.files.areaXmlFile;
      failMsg = "Failed to load area from " + fileName;
      ret.area = XmlLoadHelper.loadNewArea(fileName);

      fileName = sett.files.planesXmlFile;
      failMsg = "Failed to load plane types from " + fileName;
      ret.types = XmlLoadHelper.loadPlaneTypes(fileName);

      fileName = sett.files.fleetsXmlFile;
      failMsg = "Failed to load fleet from "+ fileName;
      ret.fleets = XmlLoadHelper.loadFleets(fileName);

    } catch (Exception ex) {
      throw new ERuntimeException("Error reading XML file " + fileName + ". " + failMsg, ex);
    }

    return ret;
  }

  static class XmlLoadedData{
    public Area area;
    public AirplaneTypes types;
    public Fleets fleets;
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
      ret = new GenericTraffic(
          sett.traffic.movementsPerHour,
          1 - sett.traffic.arrivals2departuresRatio / 10d, // 0-10 to 0.0-1.0
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

