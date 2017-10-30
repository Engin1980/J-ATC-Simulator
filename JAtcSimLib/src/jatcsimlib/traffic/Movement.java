/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.traffic;

import jatcsimlib.airplanes.Callsign;
import jatcsimlib.global.ETime;

import java.util.Comparator;

/**
 * @author Marek Vajgl
 */
public class Movement {

  public static class SortByETimeComparer implements Comparator<Movement> {

    @Override
    public int compare(Movement o1, Movement o2) {
      return o1.getInitTime().compareTo(o2.getInitTime());
    }

  }

  private final Callsign callsign;
  private final boolean departure;
  private final boolean ifr;
  private final ETime initTime;
  private final int delayInMinutes;

  public Movement(Callsign callsign, ETime initTime, int delayInMinutes, boolean isDeparture, boolean isIfr) {
    this.callsign = callsign;
    this.departure = isDeparture;
    this.initTime = initTime;
    this.ifr = isIfr;

    this.delayInMinutes = delayInMinutes;
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

  public int getDelayInMinutes() {
    return delayInMinutes;
  }

  public boolean isIfr() {
    return ifr;
  }

  @Override
  public String toString() {
    return "Movement{" + "callsign=" + callsign + ", departure=" + departure + ", ifr=" + ifr + ", initTime=" + initTime + '}';
  }

}
