/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim;

import jatcsim.startup.FrmOtherStartup;
import jatcsim.startup.FrmStartup;
import jatcsimdraw.mainRadar.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimxml.serialization.Serializer;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 *
 * @author Marek
 */
public class JAtcSim {

  private static Area area = null;
  private static Settings displaySettings = null;
  private static AirplaneTypes types = null;

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws Exception {

//    FrmOtherStartup fos = new FrmOtherStartup();
//    fos.setVisible(true);
//    
//    Thread ot = new JFrameThread(fos);
//    ot.start();
//    ot.join();
//    System.exit(1);
    
    FrmStartup fs = new FrmStartup();
    fs.eInit();
    fs.setVisible(true);
    
    System.out.println("BEF");
    Thread stf = new JFrameThread(fs);
    stf.start();
    stf.join();
    System.out.println("AFT");

//    if (fs.isDataValid() == false) {
//      System.exit(0);
//    }

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
        "C:\\Users\\Marek Vajgl\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\lkpr.xml",
        area);

      displaySettings = new Settings();
      ser.fillObject(
        "C:\\Users\\Marek Vajgl\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\mainRadarSettings.xml",
        displaySettings);

      types = new AirplaneTypes();
      ser.fillList(
        "C:\\Users\\Marek Vajgl\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\planeTypes.xml",
        types);

    } catch (Exception ex) {
      throw ex;
    }
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
