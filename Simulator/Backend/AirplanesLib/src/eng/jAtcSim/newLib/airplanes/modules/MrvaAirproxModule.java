package eng.jAtcSim.newLib.airplanes.modules;

import eng.jAtcSim.newLib.airplanes.AirproxType;

public class MrvaAirproxModule {
  private AirproxType airprox = AirproxType.none;
  private boolean mrvaError;

  public AirproxType getAirprox() {
    return this.airprox;
  }

  public void increaseAirprox(AirproxType airproxType) {
    this.airprox = AirproxType.combine(this.airprox, airproxType);
  }

  public void resetAirprox() {
    this.airprox = AirproxType.none;
  }

  public boolean isMrvaError() {
    return this.mrvaError;
  }

  public void setMrvaError(boolean value) {
    this.mrvaError = value;
  }
}
