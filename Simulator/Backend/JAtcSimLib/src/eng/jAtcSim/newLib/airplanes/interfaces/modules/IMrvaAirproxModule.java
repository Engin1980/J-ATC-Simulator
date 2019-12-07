package eng.jAtcSim.newLib.airplanes.interfaces.modules;

import eng.jAtcSim.newLib.airplanes.AirproxType;

public interface IMrvaAirproxModule {
  AirproxType getAirprox();

  boolean isMrvaError();
}
