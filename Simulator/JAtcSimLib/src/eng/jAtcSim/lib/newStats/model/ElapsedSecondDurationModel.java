package eng.jAtcSim.lib.newStats.model;

public class ElapsedSecondDurationModel {
  private static int HISTORY_LENGTH_IN_SECONDS = 60 * 10;
  private static double MEAN_INERTIA = .8;
  private int count = 0;
  private double mean = 0;
  private double max = 0;

  public void add(int duration) {
    if (count == 0) {
      // initialization
      mean = duration;
      count = 1;
    } else if (count < HISTORY_LENGTH_IN_SECONDS) {
      // adding new values
      mean = (mean * count + duration) / (count + 1);
      count++;
    } else {
      // konverging values
      mean = mean * MEAN_INERTIA + duration * (1 - MEAN_INERTIA);
    }
    if (duration > max) this.max = duration;
  }

  public double getMean() {
    return mean;
  }

  public double getMaximum() {
    return max;
  }
}
