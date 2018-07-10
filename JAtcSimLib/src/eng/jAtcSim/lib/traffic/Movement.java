/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;

import java.util.Comparator;

/**
 * @author Marek Vajgl
 */
public class Movement {


  private final Callsign callsign;
  private final AirplaneType airplaneType;
  private final boolean departure;
  private final ETime initTime;
  private final ETime appExpectedTime;
  private final int delayInMinutes;

  public Movement(Callsign callsign, AirplaneType type, ETime initTime, int delayInMinutes, boolean isDeparture) {
    this.callsign = callsign;
    this.departure = isDeparture;
    this.initTime = initTime;
    if (isDeparture) {
      this.appExpectedTime = this.initTime.addMinutes(1);
    } else {
      double appExpDelay = Acc.airport().getCoveredDistance() / (double) type.vCruise * 3600d;
      this.appExpectedTime = this.initTime.addSeconds((int) appExpDelay);
    }
    this.airplaneType = type;
    this.delayInMinutes = delayInMinutes;
  }

  private Movement() {
    callsign = null;
    airplaneType = null;
    departure = false;
    initTime = null;
    appExpectedTime = null;
    delayInMinutes = 0;
  }

  public AirplaneType getAirplaneType() {
    return airplaneType;
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

  public ETime getAppExpectedTime() {
    return appExpectedTime;
  }

  public int getDelayInMinutes() {
    return delayInMinutes;
  }

  @Override
  public String toString() {
    return "Movement{" + "callsign=" + callsign + ", departure=" + departure + ", initTime=" + initTime + '}';
  }

}
