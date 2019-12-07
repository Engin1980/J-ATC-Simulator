package eng.jAtcSim.newLib.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.airplanes.Callsign;
import eng.jAtcSim.newLib.world.Navaid;

public interface IAirplaneFlightRO {
  Callsign getCallsign();

  int getDelayDifference();

  boolean isArrival();

  default boolean isDeparture(){
    return !isArrival();
  }
}
