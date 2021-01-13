package eng.jAtcSim.newLib.airplanes.context;

import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.IAirplaneList;

public interface IAirplaneAcc {
  AirplanesController getAirplanesController();

  default IAirplaneList getAirplanes() {
    return getAirplanesController().getPlanes();
  }

  default boolean isSomeActiveEmergency() {
    return getAirplanes().isAny(q -> q.isEmergency());
  }
}
