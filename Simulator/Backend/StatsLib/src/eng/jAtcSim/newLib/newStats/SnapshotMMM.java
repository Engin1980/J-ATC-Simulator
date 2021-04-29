package eng.jAtcSim.newLib.newStats;

import exml.IXPersistable;
import exml.annotations.XConstructor;

public class SnapshotMMM implements IXPersistable, IMMM {

  private final double minimum;
  private final double maximum;
  private final double mean;

  @XConstructor
  private SnapshotMMM() {
    minimum = Double.NaN;
    maximum = Double.NaN;
    mean = Double.NaN;
  }

  public SnapshotMMM(double minimum, double maximum, double mean) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
  }

  @Override
  public double getMaximum() {
    return maximum;
  }

  @Override
  public double getMean() {
    return mean;
  }

  @Override
  public double getMinimum() {
    return minimum;
  }
}
