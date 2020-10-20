//package eng.jAtcSim.newLib.atcs.planeResponsibility.diagrams;
//
//import eng.jAtcSim.newLib.airplanes.IAirplane;
//import eng.jAtcSim.newLib.shared.AtcId;
//import eng.jAtcSim.newLib.shared.Callsign;
//import eng.jAtcSim.newLib.shared.Squawk;
//import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
//
//public class SwitchInfo {
//  private final IAirplane airplane;
//  private final AtcId atcId;
//  private EDayTimeStamp time;
//
//  public SwitchInfo(IAirplane airplane, AtcId atcId, EDayTimeStamp time) {
//    this.atcId = atcId;
//    this.airplane = airplane;
//    this.time = time;
//  }
//
//  public AtcId getAtcId() {
//    return atcId;
//  }
//
//  public IAirplane getAirplane() {
//    return airplane;
//  }
//
//  public EDayTimeStamp getTime() {
//    return time;
//  }
//
//  public void updateTime(EDayTimeStamp time) {
//    this.time = time;
//  }
//}
