/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area;

import eng.eSystem.geo.Coordinate;

/**
 *
 * @author Marek Vajgl
 */
public class VfrPoint {

  private Coordinate coordinate;
  private String name;
  private boolean forArrivals;
  private boolean forDepartures;

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public String getName() {
    return name;
  }

  public boolean isForArrivals() {
    return forArrivals;
  }

  public boolean isForDepartures() {
    return forDepartures;
  }

  @Override
  public String toString() {
    return name + " {VFR}";
  }

}
