package eng.jAtcSim.newLib.airplanes.sha;

import eng.eSystem.collections.*;

class ValueRequest {
  public double value;
  public double energy;

  public void multiply(double multiplier) {
    this.value *= multiplier;
    this.energy *= multiplier;
  }

  @Override
  public String toString() {
    return "ValueRequest{" +
        "value=" + value +
        ", energy=" + energy +
        '}';
  }
}
