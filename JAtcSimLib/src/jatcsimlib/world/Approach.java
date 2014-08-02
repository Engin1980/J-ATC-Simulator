/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.types.Coordinate;
import jatcsimlib.types.KeyItem;

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
    GPS
  }
  
  private eType type;
  private int dh;
  private int radial;
  private Coordinate point;
  
}
