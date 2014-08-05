/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import jatcsimlib.world.Border;
import jatcsimlib.world.Navaid;
import jatcsimlib.world.Runway;

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
