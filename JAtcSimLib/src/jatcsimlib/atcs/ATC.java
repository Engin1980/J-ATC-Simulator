/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

/**
 *
 * @author Marek
 */
public class ATC {
  public enum eType{
    gnd,
    twr,
    app,
    ctr
  }
  
  private final eType type;

  public ATC(eType type) {
    this.type = type;
  }

  public eType getType() {
    return type;
  }
}
