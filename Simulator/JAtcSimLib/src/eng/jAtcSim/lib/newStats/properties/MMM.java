package eng.jAtcSim.lib.newStats.properties;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
;

public class MMM {
  private double minimum;
  private double maximum;
  private double mean;

  public static MMM createMerge(IReadOnlyList<MMM> set){
    MMM ret = new MMM(
        set.minDouble(q->q.minimum),
        set.maxDouble(q->q.maximum),
        set.mean(q->q.mean));
    return ret;
  }

  @XmlConstructor
  public MMM() {
    minimum = 0;
    maximum = 0;
    mean = 0;
  }

  public MMM(double minimum, double maximum, double mean) {
    this.minimum = minimum;
    this.maximum = maximum;
    this.mean = mean;
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
