package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;

public interface IAirplane {

  IAirplaneAtc getAtc();

  Callsign getCallsign();

  Coordinate getCoordinate();

  IAirplaneFlight getFlight();

  IAirplaneRouting getRouting();

  IAirplaneSHA getSha();

  Squawk getSqwk();

  AirplaneState getState();

  AirplaneType getType();

  boolean hasElapsedEmergencyTime();

  boolean isArrival();

  default boolean isDeparture() {
    return !isArrival();
  }

  boolean isEmergency();

  GoingAroundNotification.GoAroundReason pullLastGoAroundReasonIfAny();
}
