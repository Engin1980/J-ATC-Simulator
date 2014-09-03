/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsim;

import jatcsimdraw.settings.Settings;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.AirplaneTypes;
import jatcsimlib.commands.Command;
import jatcsimlib.commands.CommandFormat;
import jatcsimxml.serialization.Serializer;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Route;
import java.util.Calendar;

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
    
    area.rebuildParentReferences();
    
    System.out.println("** Setting simulation");

    Airport aip = area.getAirports().get(0);
    final Simulation sim = Simulation.create(
        aip,
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
