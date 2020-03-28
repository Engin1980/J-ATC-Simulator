/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

import java.util.Objects;

/**
 * @author Marek
 */
public class BorderPoint {

  static class XmlLoader {
    public static BorderPoint load(XElement source) {
      Coordinate coordinate = XmlLoaderUtils.loadCoordinate(source, "coordinate");
      BorderPoint ret = new BorderPoint(coordinate);
      return ret;
    }
  }

  public static BorderPoint create(Coordinate coordinate) {
    BorderPoint ret = new BorderPoint(coordinate);
    return ret;
  }

  private final Coordinate coordinate;

  private BorderPoint(Coordinate coordinate) {
    assert coordinate != null;
    this.coordinate = coordinate;
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

  public Coordinate getCoordinate() {
    return coordinate;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + Objects.hashCode(this.coordinate);
    return hash;
  }
}
