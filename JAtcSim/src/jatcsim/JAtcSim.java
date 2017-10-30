/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim;

import jatcsim.frmPacks.Pack;
import jatcsim.startup.StartupSettings;
import jatcsim.startup.StartupWizard;
import jatcsimdraw.mainRadar.SoundManager;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimlib.traffic.CustomTraffic;
import jatcsimlib.traffic.TestTrafficOneApproach;
import jatcsimlib.traffic.Traffic;
import jatcsimlib.weathers.Weather;
import jatcsimlib.weathers.WeatherProvider;
import jatcsimxml.serialization.Serializer;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import javax.swing.JFrame;

/**
 *
 * @author Marek
 */
public class JAtcSim {

  private static final boolean FAST_START = false;

  public static java.io.File resFolder = null;
  private static Area area = null;
  private static Settings displaySettings = null;
  private static AirplaneTypes types = null;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

    initResourcesFolder();

    // startup wizard
    StartupSettings sett = StartupSettings.tryLoad();
    StartupWizard wizard = new StartupWizard(sett);
    if (FAST_START == false) {
      wizard.run();
      if (wizard.isFinished() == false) {
        return;
      }
    }

    sett.save();

    // loading data from Xml files
    try {
      loadDataFromXmlFiles(sett);
    } catch (Exception ex) {
      throw (ex);
    }

    area.initAfterLoad();

    System.out.println("** Setting simulation");

    // area, airport and time
    String icao = sett.getRecentIcao();
    Calendar simTime = Calendar.getInstance();
    updateCalendarToSimTime(simTime, sett);
    Airport aip = area.getAirports().get(icao);

    // weather
    Weather weather;
    if (sett.isWeatherOnline()) {
      weather = WeatherProvider.downloadAndDecodeMetar(aip.getIcao());
    } else {
      weather = WeatherProvider.decodeMetar(sett.getWeatherUserMetar());
    }

    // traffic
    //Traffic traffic = getTrafficFromStartupSettings(sett);
    Traffic traffic = new TestTrafficOneApproach();

    // sim init
    final Simulation sim = Simulation.create(
      aip, types, weather, traffic, simTime, sett.getSimulationSecondLengthInMs());
    SoundManager.init(resFolder.toString());

    // starting pack & simulation
    String packType = sett.getRadarPackClassName();
    jatcsim.frmPacks.Pack simPack
      = createPackInstance(packType);

    simPack.initPack(sim, area, displaySettings);
    simPack.startPack();
  }

  private static void loadDataFromXmlFiles(StartupSettings sett) throws Exception {
    System.out.println("*** Loading XML");

    Serializer ser = new Serializer();
    String failMsg;
    String fileName;

    try {
      fileName = sett.getAreaXmlFile();
      failMsg = "Failed to load area from " + fileName;
      area = Area.create();
      ser.fillObject(
        fileName,
        area);

      fileName = resFolder.toString() + "\\settings\\mainRadarSettings.xml";
      failMsg = "Failed to load area from " + fileName;
      displaySettings = new Settings();
      ser.fillObject(
        fileName,
        displaySettings);

      fileName = sett.getPlanesXmlFile();
      failMsg = "Failed to load plane types from " + fileName;
      types = new AirplaneTypes();
      ser.fillList(
        fileName,
        types);

    } catch (Exception ex) {
      throw ex;
    }
  }

  private static void initResourcesFolder() {
    String curDir = System.getProperty("user.dir") + "\\";
    java.io.File f;
    f = new java.io.File(curDir + "\\resources");
    if (f.exists()) {
      resFolder = f;
      return;
    }
    f = new java.io.File(curDir + "\\JatcSim\\resources");
    if (f.exists()) {
      resFolder = f;
      return;
    }

    throw new ERuntimeException("Unable to find resources folder.");
  }

  private static void updateCalendarToSimTime(Calendar simTime, StartupSettings sett) {
    String timeS = sett.getRecentTime();
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

  private static Traffic getTrafficFromStartupSettings(StartupSettings sett) {
    Traffic ret;
    if (sett.isTrafficUseXml()) {
      throw new UnsupportedOperationException("Traffic from XML files not supported yet.");
    } else {
      ret = new CustomTraffic(
        sett.getTrafficCustomMovements(),
        sett.getTrafficCustomArrivals2Departures() / 10d, // 0-10 to 0.0-1.0
        sett.getTrafficCustomMaxPlanes(),
        sett.getTrafficCustomVfr2Ifr() / 10d, // dtto
        sett.getTrafficCustomWeightTypeA(),
        sett.getTrafficCustomWeightTypeB(),
        sett.getTrafficCustomWeightTypeC(),
        sett.getTrafficCustomWeightTypeD(),
        sett.isTrafficCustomUsingExtendedCallsigns()
      );
    }

    return ret;
  }
}

class JFrameThread extends Thread {

  private JFrame frame;
  private final Object LOCK = new Object();

  public JFrameThread(JFrame frame) {
    this.frame = frame;
  }

  @Override
  public void run() {

    frame.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent arg) {
        synchronized (LOCK) {
          System.out.println("Unlocking");
          LOCK.notify();
        }
      }
    }
    );

    frame.setVisible(true);
    synchronized (LOCK) {
      try {
        System.out.println("Locking");
        LOCK.wait();
      } catch (InterruptedException ex) {
      }
    }
  }

}
