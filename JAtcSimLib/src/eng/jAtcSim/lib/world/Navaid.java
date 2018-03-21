/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.jAtcSim.lib.coordinates.Coordinate;
import eng.jAtcSim.lib.global.KeyItem;

/**
 *
 * @author Marek
 */
public class Navaid implements KeyItem<String> {

  @Override
  public String getKey() {
    return name;
  }
  public enum eType{
    vor,
    ndb,
    fix,
    fixMinor,
    airport,
    auxiliary
  }
  
  private Coordinate coordinate;
  private String name;
  private eType type;

  // Must be because of XML parsing
  public Navaid() {
  }

  public Navaid(String name, eType type, Coordinate coordinate) {
    this.coordinate = coordinate;
    this.name = name;
    this.type = type;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  @Override
  public String toString() {
    return name + " {" + type + '}';
  }
}
