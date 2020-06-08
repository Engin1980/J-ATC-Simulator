package eng.jAtcSim.newLib.area.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.area.airplanes.AirproxType;

public interface IMrvaAirproxModule {
  AirproxType getAirprox();

  boolean isMrvaError();
}
