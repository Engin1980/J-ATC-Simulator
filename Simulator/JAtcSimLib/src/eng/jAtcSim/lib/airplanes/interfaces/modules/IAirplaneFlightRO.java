package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.world.Navaid;

public interface IAirplaneFlightRO {
  Callsign getCallsign();

  boolean isArrival();

  default boolean isDeparture(){
    return !isArrival();
  }
}
