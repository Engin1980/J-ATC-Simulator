package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.collections.IReadOnlyList;

public class MMM {
  public static MMM createMerge(IReadOnlyList<MMM> set) {
    MMM ret = new MMM(
        set.minDouble(q -> q.minimum),
        set.maxDouble(q -> q.maximum),
        set.mean(q -> q.mean));
    return ret;
  }
  private final double minimum;
  private final double maximum;
  private final double mean;

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
