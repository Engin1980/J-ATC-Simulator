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
public class Approach implements KeyItem<Approach.eType> {

  @Override
  public eType getKey() {
    return type;
  }
 
  public enum eType{
    ILS_I,
    ILS_II,
    ILS_III,
    VORDME,
    NDB,
    GPS,
    Visual
  }
  
  private eType type;
  private int dh;
  private int radial;
  private Coordinate point;
  private String gaRoute;
  private RunwayThreshold parent;

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }

  public String getGaRoute() {
    return gaRoute;
  }

  public eType getType() {
    return type;
  }

  public int getDh() {
    return dh;
  }

  public int getRadial() {
    return radial;
  }

  public Coordinate getPoint() {
    return point;
  }
 
  
}
  
