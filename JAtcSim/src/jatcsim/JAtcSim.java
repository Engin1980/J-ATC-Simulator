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
import javax.swing.JFrame;
import javax.swing.Timer;

/**
 *
 * @author Marek
 */
public class JAtcSim {

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {

    System.out.println("*** Loading XML");
    
    Serializer ser = new Serializer();
    Area area = null;
    Settings displaySettings = null;
    AirplaneTypes types = null;
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

    System.out.println("** Setting simulation");
    
    Airport aip = area.getAirports().get(0);
    final Simulation sim = new Simulation(
      area, aip,
      types, Calendar.getInstance());

    // CR
    //Coordinate tl = new Coordinate(51.1, 12.0);
    //Coordinate br = new Coordinate(48.5, 18.9);
    
    System.out.println("*** Init of canvas");
    
    EJComponentCanvas canvas = new EJComponentCanvas();
    Radar r = new Radar(canvas, aip.getRadarRange(), sim, displaySettings);
    final EJComponent comp = canvas.getEJComponent();

    final FrmMain f = new FrmMain();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    f.setSize(900, 500);
    comp.setSize(900, 500);
    // anchor of radar:
    f.addComponentListener(new ComponentListener() {

      @Override
      public void componentResized(ComponentEvent e) {
        comp.setSize(f.getWidth(), f.getHeight());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {
      }
    });
    f.add(comp);
    f.setVisible(true);

    int delay = 3000; //milliseconds
    ActionListener taskPerformer = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent evt) {
        sim.elapseSecond();
        comp.repaint();
      }
    };

    // mouse coord on title
    r.onMouseMove().addListener(new EventListener<Radar, WithCoordinateEvent>() {

      @Override
      public void raise(Radar parent, WithCoordinateEvent e) {
        f.setTitle(e.coordinate.toString());
      }
    });

    new Timer(delay, taskPerformer).start();
  }
}
