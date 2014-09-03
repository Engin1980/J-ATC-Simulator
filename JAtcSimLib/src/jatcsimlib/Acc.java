/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib;

import jatcsimlib.airplanes.AirplaneList;
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

  public static Simulation sim() {
    return Simulation.getCurrent();
  }
  
  public static AirplaneList planes(){
    return Simulation.getCurrent().getPlanes();
  }
  
  public static ETime now(){
    return Simulation.getCurrent().getNow();
  }

  public static Airport airport() {
    return Simulation.getCurrent().getActiveAirport();
  }

  public static Messenger messenger() {
    return Simulation.getCurrent().getMessenger();
  }

  public static Weather weather() {
    throw new ENotSupportedException();
//return Simulation.getCurrent().getWeather();
  }

  public static RunwayThreshold threshold() {
    return Simulation.getCurrent().getActiveRunwayThreshold();
  }

  public static Area area() {
    return Simulation.getCurrent().getArea();
  }
  
  public static UserAtc atcApp(){
    return Simulation.getCurrent().getAppAtc();
  }
  public static TowerAtc atcTwr(){
    return Simulation.getCurrent().getTwrAtc();
  }
  public static CentreAtc atcCtr(){
    return Simulation.getCurrent().getCtrAtc();
  }
}
