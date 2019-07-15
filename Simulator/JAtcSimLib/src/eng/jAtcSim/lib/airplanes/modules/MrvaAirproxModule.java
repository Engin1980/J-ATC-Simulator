package eng.jAtcSim.lib.airplanes.modules;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirproxType;

public class MrvaAirproxModule {
  private final Airplane parent;
  private AirproxType airprox = AirproxType.none;
  private boolean mrvaError;

  public MrvaAirproxModule(Airplane parent) {
    this.parent = parent;
  }

  public AirproxType getAirprox() {
    return this.airprox;
  }

  public void increaseAirprox(AirproxType at) {
    this.airprox = AirproxType.combine(this.airprox, at);
  }

  public void resetAirprox() {
    this.airprox = AirproxType.none;
  }

  public boolean isMrvaError() {
    return this.mrvaError;
  }
}
