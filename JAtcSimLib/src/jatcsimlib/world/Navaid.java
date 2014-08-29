/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;

/**
 *
 * @author Marek
 */
public class Navaid implements KeyItem<String> {

  public static Area area;
  
  @Override
  public String getKey() {
    return name;
  }
  public enum eType{
    VOR,
    NDB,
    Fix,
    FixMinor,
    Airport
  }
  
  private Coordinate coordinate;
  private String name;
  private eType type;

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }
  
}
