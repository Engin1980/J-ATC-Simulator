package eng.jAtcSim.newLib.airplanes.context;

import eng.jAtcSim.newLib.airplanes.AirplaneList;
import eng.jAtcSim.newLib.airplanes.AirplanesController;
import eng.jAtcSim.newLib.airplanes.IAirplane;

public interface IAirplaneAcc {
  default AirplaneList<IAirplane> getAirplanes() {
    return getAirplanesController().getPlanes();
  }
  AirplanesController getAirplanesController();
  default boolean isSomeActiveEmergency() {
    return getAirplanes().isAny(q -> q.isEmergency());
  }
}
