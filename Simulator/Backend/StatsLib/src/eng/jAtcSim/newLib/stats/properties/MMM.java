package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.PostContracts;

import exml.IXPersistable;
import exml.annotations.XConstructor;

public class MMM implements IXPersistable {
  public static MMM createMerge(IReadOnlyList<MMM> set) {
    MMM ret = new MMM(
            set.minDouble(q -> q.minimum).orElse(0),
            set.maxDouble(q -> q.maximum).orElse(0),
            set.mean(q -> q.mean).orElse(0));
    return ret;
  }

  private final double minimum;
  private final double maximum;
  private final double mean;

  @XConstructor

  private MMM() {
    minimum = Double.NaN;
    maximum = Double.NaN;
    mean = Double.NaN;

//    PostContracts.register(this, () -> !Double.isNaN(minimum));
  }

  public MMM(double minimum, double maximum, double mean) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
  }

  public double getMaximum() {
    return maximum;
  }

  public double getMean() {
    return mean;
  }

  public double getMinimum() {
    return minimum;
  }
}
