package eng.jAtcSim.lib.newStats.properties;

import eng.eSystem.xmlSerialization.annotations.XmlConstructor;

public class MMM {
  private final double minimum;
  private final double maximum;
  private final double mean;

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
