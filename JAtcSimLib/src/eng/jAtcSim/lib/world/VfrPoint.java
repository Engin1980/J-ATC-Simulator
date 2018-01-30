/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.KeyItem;
import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;

/**
 *
 * @author Marek Vajgl
 */
public class VfrPoint implements KeyItem<String> {

  private Coordinate coordinate;
  private String name;
  private boolean forArrivals;
  private boolean forDepartures;

  @Override
  public String getKey() {
    return name;
  }

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
