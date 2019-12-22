package eng.jAtcSim.newLib.area.airplanes.modules;

import eng.jAtcSim.newLib.area.airplanes.AirproxType;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.interfaces.modules.IMrvaAirproxModule;

public class MrvaAirproxModule extends Module implements IMrvaAirproxModule {
  private AirproxType airprox = AirproxType.none;
  private boolean mrvaError;

  public MrvaAirproxModule(IAirplaneWriteSimple parent) {
    super(parent);
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

  public void setMrvaError(boolean value) {
    this.mrvaError = value;
  }
}
