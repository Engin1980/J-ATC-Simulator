/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib;

import eng.eSystem.ERandom;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.AirplaneTypes;
import eng.jAtcSim.newLib.area.atcs.Atc;
import eng.jAtcSim.newLib.area.atcs.CenterAtc;
import eng.jAtcSim.newLib.area.atcs.TowerAtc;
import eng.jAtcSim.newLib.area.atcs.UserAtc;
import eng.jAtcSim.newLib.area.atcs.planeResponsibility.PlaneResponsibilityManager;
import eng.jAtcSim.newLib.messaging.Messenger;
import eng.jAtcSim.newLib.area.newStats.StatsManager;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.global.logging.ApplicationLog;
import eng.jAtcSim.newLib.traffic.Movement;
import eng.jAtcSim.newLib.traffic.fleets.Fleets;
import eng.jAtcSim.newLib.weathers.Weather;
import eng.jAtcSim.newLib.world.Airport;
import eng.jAtcSim.newLib.world.Area;

/**
 * @author Marek
 */
public class Acc {

  private static Area area;
  private static Simulation sim;
  private static Airport aip;
  private static ApplicationLog log;

  public static void setLog(ApplicationLog log){
   Acc.log = log;
  }

  public static ApplicationLog log(){
    return log;
  }

  public static void setArea(Area area) {
    Acc.area = area;
  }

  public static void setAirport(Airport aip) {
    Acc.aip = aip;
  }

  public static void setSimulation(Simulation simulation) {
    Acc.sim = simulation;
    Acc.setAirport(sim.getActiveAirport());
  }

  public static Simulation sim() {
    return sim;
  }

  public static IReadOnlyList<Airplane> planes() {
    return prm().getPlanes();
  }

  public static PlaneResponsibilityManager prm() {
    return sim.getPrm();
  }

  public static ETime now() {
    return sim.getNow();
  }

  public static Airport airport() {
    return Acc.aip;
  }

  public static Messenger messenger() {
    return sim().getMessenger();
  }

  public static Weather weather() {
    return sim.getWeather();
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

  public static CenterAtc atcCtr() {
    return sim.getCtrAtc();
  }

  public static String toAltS(double altitudeInFt, boolean appendFt) {
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
        throw new EEnumValueUnsupportedException(type);
    }
  }

  public static ERandom rnd() {
    return Simulation.rnd;
  }

  public static StatsManager stats() {
    return sim().getStats();
  }

  public static AirplaneTypes types() {
    return sim.getAirplaneTypes();
  }

  public static Fleets fleets() {
    return sim.getFleets();
  }

  public static IReadOnlyList<Movement> scheduledMovements(){
    return sim.getTrafficManager().getScheduledMovements();
  }

  public static boolean isSomeActiveEmergency() {
    boolean ret = Acc.planes().isAny(q->q.getEmergencyModule().isEmergency() && q.getFlightModule().isArrival());
    return ret;
  }
}
