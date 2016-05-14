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
  private final Callsign callsign;
  private final boolean departure;
  private final boolean ifr;
  private final ETime initTime;

  public Movement(Callsign callsign, ETime initTime, boolean isDeparture, boolean isIfr) {
    this.callsign = callsign;
    this.departure = isDeparture;
    this.initTime = initTime;
    this.ifr = isIfr;
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

  public boolean isIfr() {
    return ifr;
  }

  @Override
  public String toString() {
    return "Movement{" + "callsign=" + callsign + ", departure=" + departure + ", ifr=" + ifr + ", initTime=" + initTime + '}';
  }
  
}
