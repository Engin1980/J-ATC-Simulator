/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.radar;

import jatcismdraw.radarBase.Visualiser;
import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.messaging.Message;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Border;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Runway;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class PaintManager {

  private final Simulation simulation;
  private final Visualiser visualiser;

  public PaintManager(Simulation simulation, Visualiser visualiser) {
    this.simulation = simulation;
    this.visualiser = visualiser;
  }

  public void draw() {
    drawBackground();
    drawBorders();
    drawNavaids();
    drawAirports();
    drawAirplanes();
    drawCaptions();
  }
  
  private void drawCaptions(){
    Messenger ms = simulation.getMessenger();
    List<Message> msgs = ms.getMy(simulation.getAppAtc(), false);
    visualiser.drawMessages(msgs);
  }

  private void drawBorders() {
    for (Border b : simulation.getArea().getBorders()){
      visualiser.drawBorder(b);
    }
  }

  private void drawNavaids() {
    for (Navaid n : simulation.getArea().getNavaids()){
      visualiser.drawNavaid(n);
    }
  }

  private void drawAirports() {
    for(Airport a : simulation.getArea().getAirports()){
      drawAirport(a);
    }
  }

  private void drawAirport(Airport a) {
    for(Runway r : a.getRunways()){
      visualiser.drawRunway(r);
    }
  }

  private void drawBackground() {
    visualiser.clear();
  }

  private void drawAirplanes() {
    for (Airplane a : simulation.getPlanes()){
      visualiser.drawPlane(a);
    }
  }
}