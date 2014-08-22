/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim;

import jatcsimdraw.painting.EJComponent;
import jatcsimdraw.painting.EJComponentCanvas;
import jatcsimdraw.painting.Radar;
import jatcsimdraw.painting.Settings;
import jatcsimlib.events.EventListener;
import jatcsimdraw.shared.es.WithCoordinateEvent;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimxml.serialization.Serializer;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.Timer;

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
    try {
      loadDataFromXmlFiles();
    } catch (Exception ex) {
      throw (ex);
    }

    System.out.println("** Setting simulation");

    Airport aip = area.getAirports().get(0);
    final Simulation sim = new Simulation(
        area, aip,
        types, Calendar.getInstance());

    jatcsim.frmPacks.Pack simPack = 
        new jatcsim.frmPacks.oneWindow.Pack();
    
    simPack.initPack(sim, displaySettings);
  }

  private static void loadDataFromXmlFiles() throws Exception {
    System.out.println("*** Loading XML");
    
    Serializer ser = new Serializer();
    
    try {
      area = new Area();
      ser.fillObject(
          "C:\\Users\\Marek\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\lkpr.xml",
          area);
      
      displaySettings = new Settings();
      ser.fillObject(
          "C:\\Users\\Marek\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\settings.xml",
          displaySettings);
      
      types = new AirplaneTypes();
      ser.fillList(
          "C:\\Users\\Marek\\Documents\\NetBeansProjects\\_JAtcSimSolution\\JAtcSim\\src\\jatcsim\\planeTypes.xml",
          types);
      
    } catch (Exception ex) {
      throw ex;
    }
  }
}
