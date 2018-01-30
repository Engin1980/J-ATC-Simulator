/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import jatcsimlib.coordinates.Coordinate;
import java.util.Objects;

/**
 *
 * @author Marek
 */
public class BorderArcPoint extends BorderPoint {
  
  public enum eDirection {
    clockwise,
    counterclockwise
  }
  
  private Coordinate coordinate;
  private eDirection direction;

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public eDirection getDirection() {
    return direction;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 67 * hash + Objects.hashCode(this.coordinate);
    hash = 67 * hash + Objects.hashCode(this.direction);
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
    final BorderArcPoint other = (BorderArcPoint) obj;
    if (!Objects.equals(this.coordinate, other.coordinate)) {
      return false;
    }
    if (this.direction != other.direction) {
      return false;
    }
    return true;
  }
}
