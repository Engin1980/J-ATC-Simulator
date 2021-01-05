package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.validation.EAssert;
import exml.IXPersistable;

public class StatisticProperty implements IXPersistable {

  private double minimum;
  private double maximum;
  private double sum;
  private int count;

  public StatisticProperty() {
    minimum = Double.NaN;
    maximum = Double.NaN;
    sum = 0;
    count = 0;
  }

  public void add(double value) {
    adjustMinimumAndMaximum(value);
    sum += value;
    count++;
  }

  public void add(double value, int count) {
    EAssert.isTrue(count > 0);
    adjustMinimumAndMaximum(value);
    this.sum += value * count;
    this.count += count;
  }

  public StatisticProperty createMerge(StatisticProperty other) {
    StatisticProperty ret = new StatisticProperty();
    ret.minimum = Math.min(this.minimum, other.minimum);
    ret.maximum = Math.max(this.maximum, other.maximum);
    ret.sum = this.sum + other.sum;
    ret.count = this.count + other.count;
    return ret;
  }

  public int getCount() {
    return count;
  }

  public double getMaximum() {
    return maximum;
  }

  public double getMean() {
    if (count == 0)
      return Double.NaN;
    else
      return sum / count;
  }

  public double getMinimum() {
    return minimum;
  }

  public double getSum() {
    return sum;
  }

  public MMM toMMM() {
    MMM ret = new MMM(this.getMinimum(), this.getMaximum(), this.getMean());
    return ret;
  }

  private void adjustMinimumAndMaximum(double value) {
    if (Double.isNaN(minimum) || minimum > value)
      minimum = value;
    if (Double.isNaN(maximum) || maximum < value)
      maximum = value;
  }
}
