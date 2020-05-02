package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;

public interface IAirplane {

  IAirplaneAtc getAtc();

  Callsign getCallsign();

  Coordinate getCoordinate();

  IAirplaneRouting getRouting();

  IAirplaneSHA getSha();

  Squawk getSqwk();

  AirplaneState getState();

  AirplaneType getType();

  boolean isArrival();

  default boolean isDeparture() {
    return !isArrival();
  }

  boolean isEmergency();
}
