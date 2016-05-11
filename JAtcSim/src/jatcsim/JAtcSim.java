/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim;

import jatcsim.startup.FrmStartup;
import jatcsim.startup.StartupSettings;
import jatcsim.startup.StartupWizard;
import jatcsimdraw.mainRadar.SoundManager;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.exceptions.ERuntimeException;
import jatcsimxml.serialization.Serializer;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import javax.swing.JFrame;

/**
 *
 * @author Marek
 */
public class JAtcSim {

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
    wizard.run();
    if (wizard.isFinished() == false)
      return;
    sett.save();
    
    
    /*
    FrmStartup fs = new FrmStartup();
    fs.eInit();
    fs.setVisible(true);
    
    System.out.println("BEF");
    Thread stf = new JFrameThread(fs);
    stf.start();
    stf.join();
    System.out.println("AFT");
    */
      
    // loading data
    try {
      loadDataFromXmlFiles();
    } catch (Exception ex) {
      throw (ex);
    }

    area.initAfterLoad();

    System.out.println("** Setting simulation");

    Airport aip = area.getAirports().get(0);
    final Simulation sim = Simulation.create(
      aip,
      types, Calendar.getInstance());
    SoundManager.init(resFolder.toString());

    // starting pack & simulation
    jatcsim.frmPacks.Pack simPack
      = new jatcsim.frmPacks.simple.Pack();

    simPack.initPack(sim, area, displaySettings);
    simPack.startPack();
  }

  private static void loadDataFromXmlFiles() throws Exception {
    System.out.println("*** Loading XML");

    Serializer ser = new Serializer();

    try {
      area = Area.create();
      ser.fillObject(
        resFolder.toString() + "\\areas\\lkpr.xml",
        area);

      displaySettings = new Settings();
      ser.fillObject(
        resFolder.toString() + "\\settings\\mainRadarSettings.xml",
        displaySettings);

      types = new AirplaneTypes();
      ser.fillList(
        resFolder.toString() + "\\settings\\planeTypes.xml",
        types);

    } catch (Exception ex) {
      throw ex;
    }
  }

  private static void initResourcesFolder() {
    String curDir = System.getProperty("user.dir") + "\\";
    java.io.File f;
    f = new java.io.File(curDir + "src\\resources");
    if (f.exists()){
      resFolder = f;
      return;
    }
    f = new java.io.File(curDir + "resources");
    if (f.exists()){
      resFolder = f;
      return;
    }
    
    throw new ERuntimeException("Unable to find resources folder.");
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
