/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.airplanes.AirplaneList;
import jatcsimlib.atcs.Atc;
import jatcsimlib.atcs.CentreAtc;
import jatcsimlib.atcs.TowerAtc;
import jatcsimlib.atcs.UserAtc;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.ETime;
import jatcsimlib.messaging.Messenger;
import jatcsimlib.weathers.Weather;
import jatcsimlib.world.Airport;
import jatcsimlib.world.Area;
import jatcsimlib.world.RunwayThreshold;

/**
 *
 * @author Marek
 */
public class Acc {

  private static Area area;
  private static Simulation sim;

  public static void setArea(Area area) {
    Acc.area = area;
  }

  public static void setSimulation(Simulation simulation) {
    Acc.sim = simulation;
  }

  public static Simulation sim() {
    return sim;
  }

  public static AirplaneList planes() {
    return sim.getPlanes();
  }

  public static ETime now() {
    return sim.getNow();
  }

  public static Airport airport() {
    return sim.getActiveAirport();
  }

  public static Messenger messenger() {
    return sim.getMessenger();
  }

  public static Weather weather() {
    throw new ENotSupportedException();
//return sim.getWeather();
  }

  public static RunwayThreshold threshold() {
    return sim.getActiveRunwayThreshold();
  }

  public static Area area() {
    return area;
  }

  public static UserAtc atcApp() {
    return sim.getAppAtc();
  }

  public static TowerAtc atcTwr() {
    return sim.getTwrAtc();
  }

  public static CentreAtc atcCtr() {
    return sim.getCtrAtc();
  }

  public static String toAltS(int altitudeInFt, boolean appendFt) {
    return Acc.sim().toAltitudeString(altitudeInFt, appendFt);
  }

  public static Atc atc(Atc.eType type) {
    switch (type) {
      case app:
        return atcApp();
      case ctr:
        return atcCtr();
      case twr:
        return atcTwr();
      default:
        throw new ENotSupportedException();
    }
  }
}
