package eng.jAtcSim.lib.newStats.properties;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;

public class MMM {
  private double minimum;
  private double maximum;
  private double mean;
  private int count;

  public static MMM merge(MMM a, MMM b) {
    MMM ret = new MMM();
    ret.minimum = Math.min(a.getMinimum(), b.getMinimum());
    ret.maximum = Math.max(a.getMaximum(), b.getMaximum());
    ret.mean = (a.mean * a.count + b.mean * b.count) / (a.count + b.count);
    return ret;
  }

  public static MMM merge(IReadOnlyList<MMM> mmms) {
    MMM ret = new MMM();
    boolean isFirst = true;
    double sum = 0;
    int cnt = 0;
    for (MMM mmm : mmms) {
      if (isFirst) {
        ret.minimum = mmm.minimum;
        ret.maximum = mmm.maximum;
      } else {
        if (ret.minimum > mmm.minimum) ret.minimum = mmm.minimum;
        if (ret.maximum < mmm.maximum) ret.maximum = mmm.maximum;
      }
      sum += mmm.mean * mmm.count;
      cnt += mmm.count;
    }

    ret.mean = sum / cnt;
    ret.count = cnt;

    return ret;
  }

  @XmlConstructor
  public MMM() {
    minimum = 0;
    maximum = 0;
    mean = 0;
  }

  public MMM(double minimum, double maximum, double mean, int count) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
    this.count = count;
  }

  public double getMinimum() {
    return minimum;
  }

  public double getMaximum() {
    return maximum;
  }

  public double getMean() {
    return mean;
  }
}
