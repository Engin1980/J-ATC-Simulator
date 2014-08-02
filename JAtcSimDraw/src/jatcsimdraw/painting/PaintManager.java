/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

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

  private final Area area;
  private final Visualiser visualiser;

  public PaintManager(Area area, Visualiser visualiser) {
    this.area = area;
    this.visualiser = visualiser;
  }

  public void draw() {
    drawBackground();
    drawBorders();
    drawNavaids();
    drawAirports();
  }

  private void drawBorders() {
    for (Border b : area.getBorders()){
      visualiser.drawBorder(b);
    }
  }

  private void drawNavaids() {
    for (Navaid n : area.getNavaids()){
      visualiser.drawNavaid(n);
    }
  }

  private void drawAirports() {
    for(Airport a : area.getAirports()){
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
}
