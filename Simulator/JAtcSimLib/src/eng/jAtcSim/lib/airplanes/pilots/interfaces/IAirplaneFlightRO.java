package eng.jAtcSim.lib.airplanes.pilots.interfaces;

import eng.jAtcSim.lib.airplanes.Callsign;

public interface IAirplaneFlightRO {
  Callsign getCallsign();

  boolean isArrival();
}
