/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.geo.Coordinate;

import java.util.Objects;

/**
 *
 * @author Marek
 */
public class BorderPoint {
  private Coordinate coordinate;

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public BorderPoint(Coordinate coordinate) {
    this.coordinate = coordinate;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.coordinate);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final BorderPoint other = (BorderPoint) obj;
    if (!Objects.equals(this.coordinate, other.coordinate)) {
      return false;
    }
    return true;
  }
}
