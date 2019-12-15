///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package eng.jAtcSim.newLib.traffic;
//
//import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
//import eng.jAtcSim.newLib.Acc;
//import eng.jAtcSim.newLib.airplanes.AirplaneType;
//import eng.jAtcSim.newLib.airplanes.Callsign;
//import eng.jAtcSim.newLib.global.ETime;
//
//;
//
///**
// * @author Marek Vajgl
// */
//public class Movement {
//
//  private final Callsign callsign;
//  private final AirplaneType airplaneType;
//  private final boolean departure;
//  private final ETime initTime;
//  private final ETime appExpectedTime;
//  private int delayInMinutes;
//  private final int entryRadial;
//
//  public Movement(Callsign callsign, AirplaneType type, ETime initTime, int delayInMinutes, boolean isDeparture, int entryRadial) {
//    this.callsign = callsign;
//    this.departure = isDeparture;
//    this.initTime = initTime;
//    this.entryRadial = entryRadial;
//    if (isDeparture) {
//      this.appExpectedTime = this.initTime.addMinutes(1);
//    } else {
//      double appExpDelay = Acc.airport().getCoveredDistance() / (double) type.vCruise * 3600d;
//      this.appExpectedTime = this.initTime.addSeconds((int) appExpDelay);
//    }
//    this.airplaneType = type;
//    this.delayInMinutes = delayInMinutes;
//  }
//
//  @XmlConstructor
//  private Movement() {
//    callsign = null;
//    airplaneType = null;
//    departure = false;
//    initTime = null;
//    appExpectedTime = null;
//    delayInMinutes = 0;
//    entryRadial = 0;
//  }
//
//  public int getEntryRadial() {
//    return entryRadial;
//  }
//
//  public AirplaneType getAirplaneType() {
//    return airplaneType;
//  }
//
//  public Callsign getCallsign() {
//    return callsign;
//  }
//
//  public boolean isDeparture() {
//    return departure;
//  }
//
//  public ETime getInitTime() {
//    return initTime;
//  }
//
//  public ETime getAppExpectedTime() {
//    return appExpectedTime;
//  }
//
//  public int getDelayInMinutes() {
//    return delayInMinutes;
//  }
//
//  @Override
//  public String toString() {
//    return "Movement{" + "callsign=" + callsign + ", departure=" + departure + ", initTime=" + initTime + '}';
//  }
//
//  public void clearDelayMinutes() {
//    this.delayInMinutes = 0;
//  }
//}
