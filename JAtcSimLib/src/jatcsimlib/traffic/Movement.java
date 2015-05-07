/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import jatcsimlib.airplanes.Callsign;
import jatcsimlib.global.ETime;

/**
 *
 * @author Marek Vajgl
 */
public class Movement {
  private Callsign callsign;
  private boolean departure;
  private ETime initTime;

  public Movement(Callsign callsign, ETime initTime, boolean isDeparture) {
    this.callsign = callsign;
    this.departure = isDeparture;
    this.initTime = initTime;
  }    

  public Callsign getCallsign() {
    return callsign;
  }

  public boolean isDeparture() {
    return departure;
  }

  public ETime getInitTime() {
    return initTime;
  }
  
  
}
