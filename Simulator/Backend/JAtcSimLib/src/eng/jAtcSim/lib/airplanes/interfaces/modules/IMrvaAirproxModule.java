package eng.jAtcSim.lib.airplanes.interfaces.modules;

import eng.jAtcSim.lib.airplanes.AirproxType;

public interface IMrvaAirproxModule {
  AirproxType getAirprox();

  boolean isMrvaError();
}
