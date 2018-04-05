/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.ReadOnlyList;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirplaneTypes;
import eng.jAtcSim.lib.atcs.*;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.global.ERandom;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.global.logging.ApplicationLog;
import eng.jAtcSim.lib.global.logging.Log;
import eng.jAtcSim.lib.managers.MrvaManager;
import eng.jAtcSim.lib.stats.Statistics;
import eng.jAtcSim.lib.traffic.fleets.Fleets;
import eng.jAtcSim.lib.weathers.Weather;
import eng.jAtcSim.lib.weathers.WeatherProvider;
import eng.jAtcSim.lib.world.Airport;
import eng.jAtcSim.lib.world.Area;
import eng.jAtcSim.lib.world.RunwayThreshold;

import java.util.List;

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

  public static ReadOnlyList<Airplane> planes() {
    return prm().getAll();
  }

  public static PlaneResponsibilityManager prm() {
    return PlaneResponsibilityManager.getInstance();
  }

  public static ETime now() {
    return sim.getNow();
  }

  public static Airport airport() {
    return Acc.aip;
  }

  public static eng.jAtcSim.lib.messaging.Messenger messenger() {
    return sim().getMessenger();
  }

  public static Weather weather() {
    return sim.getWeather();
  }

  public static IList<RunwayThreshold> thresholds() {
    return atcTwr().getRunwayThresholdsInUse();
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
        throw new ENotSupportedException();
    }
  }

  public static ERandom rnd() {
    return Simulation.rnd;
  }

  public static Statistics stats() {
    return sim().getStats();
  }

  public static AirplaneTypes types() {
    return sim.getPlaneTypes();
  }

  public static Fleets fleets() {
    return sim.getFleets();
  }

  public static WeatherProvider weatherProvider() {
    return sim.getWeatherProvider();
  }
}
