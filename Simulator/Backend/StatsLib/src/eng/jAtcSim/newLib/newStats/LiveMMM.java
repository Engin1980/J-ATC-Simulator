package eng.jAtcSim.newLib.newStats;

import exml.IXPersistable;

public class LiveMMM implements IXPersistable, IMMM {
  private static ElapsedSecondCounter elapsedSecondCounter;

  public static void setElapsedSecondCounter(ElapsedSecondCounter elapsedSecondCounter) {
    LiveMMM.elapsedSecondCounter = elapsedSecondCounter;
  }

  private double minimum;
  private double maximum;
  private double sum;
  private double current;

  public void addValue(double value) {
    if (elapsedSecondCounter.get() == 0) {
      this.minimum = this.maximum = this.sum = value;
    } else {
      if (value < minimum)
        minimum = value;
      if (value > maximum)
        maximum = value;
      sum += value;
    }
    this.current = value;
  }

  public double getCurrent() {
    return current;
  }

  @Override
  public double getMaximum() {
    return this.maximum;
  }

  @Override
  public double getMean() {
    return this.sum / elapsedSecondCounter.get();
  }

  @Override
  public double getMinimum() {
    return this.minimum;
  }
}
