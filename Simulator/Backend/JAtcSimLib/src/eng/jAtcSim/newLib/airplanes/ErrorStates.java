package eng.jAtcSim.newLib.airplanes;

public class ErrorStates {
  private AirproxType airprox;
  private boolean mrvaError;

  public void setAirprox(AirproxType airprox) {
    this.airprox = airprox;
  }

  public void resetAirprox() {
    this.airprox = AirproxType.none;
  }

  public void increaseAirprox(AirproxType at) {
    this.airprox = AirproxType.combine(this.airprox, at);
  }

  public boolean isMrvaError() {
    return mrvaError;
  }

  public void setMrvaError(boolean mrvaError) {
    this.mrvaError = mrvaError;
  }
}
