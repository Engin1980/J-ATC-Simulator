/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.atcs;

import jatcsimlib.Simulation;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.messaging.Messenger;

/**
 *
 * @author Marek
 */
public abstract class Atc {
  
  protected static final String UNRECOGNIZED = "???";
  
  public enum eType{
    gnd,
    twr,
    app,
    ctr
  }
  
  private final eType type;
  
  private final String name;

  public Atc(eType type, String icao) {
    this.type = type;
    switch (type){
      case app:
        this.name = icao + "_APP";
        break;
      case ctr:
        this.name = icao + "_CTR";
        break;
      case twr:
        this.name = icao + "_TWR";
        break;
      case gnd:
        this.name = icao + "_GND";
        break;
      default:
        throw new ENotSupportedException();
    }
  }
  
  public abstract boolean isHuman();

  public eType getType() {
    return type;
  }

  public String getName() {
    return name;
  }
  
  public void registerNewPlane (Airplane plane){
    plane.setAtcOnlyAtcCanCallThis(this);
    _registerNewPlane(plane);
  }
  protected abstract void _registerNewPlane(Airplane plane);
 
}
