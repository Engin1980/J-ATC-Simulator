/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.types.Coordinate;

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
  
  
}
