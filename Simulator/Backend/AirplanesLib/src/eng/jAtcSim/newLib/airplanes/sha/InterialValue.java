package eng.jAtcSim.newLib.airplanes.sha;

class InertialValue {
  private final double maxPositiveInertiaChange;
  private final double maxNegativeInertiaChange;
  private double value;
  private double inertia;
  private Double minimum;

  public InertialValue(double value,
                       double maxPositiveInertiaChange, double maxNegativeInertiaChange,
                       Double minimumOrNull) {
    this.value = value;
    this.inertia = 0;
    this.minimum = minimumOrNull;
    this.maxPositiveInertiaChange = maxPositiveInertiaChange;
    this.maxNegativeInertiaChange = maxNegativeInertiaChange;
  }

  public void add(double val) {
    double adjustedValue;
    if (val > inertia)
      adjustedValue = Math.min(val, inertia + maxPositiveInertiaChange);
    else
      adjustedValue = Math.max(val, inertia - maxNegativeInertiaChange);

    this.inertia = adjustedValue;
    this.value += this.inertia;

    if ((this.minimum != null) && (this.value < this.minimum)) {
      this.value = this.minimum;
      this.inertia = 0;
    }
  }

  public double getInertia() {
    return inertia;
  }

  public double getMaxNegativeInertiaChange() {
    return maxNegativeInertiaChange;
  }

  public double getMaxPositiveInertiaChange() {
    return maxPositiveInertiaChange;
  }

  public double getValue() {
    return value;
  }

  public void reset(double value) {
    this.value = value;
    this.inertia = 0;
  }

  public void set(double value) {
    double diff = value - this.value;
    this.add(diff);
  }
}
