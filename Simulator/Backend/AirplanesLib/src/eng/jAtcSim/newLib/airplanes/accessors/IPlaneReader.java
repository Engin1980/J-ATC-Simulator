//package eng.jAtcSim.newLib.airplanes.accessors;
//
//import eng.eSystem.geo.Coordinate;
//import eng.jAtcSim.newLib.airplaneType.AirplaneType;
//import eng.jAtcSim.newLib.airplanes.internal.Airplane;
//import eng.jAtcSim.newLib.area.Navaid;
//import eng.jAtcSim.newLib.shared.AtcId;
//import eng.jAtcSim.newLib.shared.Callsign;
//import eng.jAtcSim.newLib.shared.Restriction;
//
//public interface IPlaneReader {
//  int getAltitude();
//
//  Callsign getCallsign();
//
//  char getCategory();
//
//  Coordinate getCoordinate();
//
//  Navaid getEntryExitPoint();
//
//  int getHeading();
//
//  int getSpeed();
//
//  Restriction getSpeedRestriction();
//
//  AirplaneState getState();
//
//  int getTargetAltitude();
//
//  int getTargetHeading();
//
//  AtcId getTunedAtc();
//
//  AirplaneType getType();
//
//  /* from .getRoutingModule() */
//  boolean hasLateralDirectionAfterCoordinate();
//
//  boolean hasRadarContact();
//
//  boolean isArrival();
//
//  boolean isDeparture();
//
//  boolean isDivertable();
//
//  boolean isEmergency();
//
//  boolean isGoingToFlightOverNavaid(Navaid n);
//
//  boolean isRoutingEmpty();
//
//  Coordinate tryGetTargetCoordinate();
//
//  /*
//  if (targetCoordinate == null
//        && parent.getBehaviorModule().is(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class)) {
//      eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior hb = parent.getBehaviorModule().getAs(eng.jAtcSim.newLib.area.airplanes.behaviors.HoldBehavior.class);
//      targetCoordinate = hb.navaid.getCoordinate();
//    }
//   */
//  Coordinate tryGetTargetOrHoldCoordinate();
//}
