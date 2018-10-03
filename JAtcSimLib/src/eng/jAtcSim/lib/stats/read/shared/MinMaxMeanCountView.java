package eng.jAtcSim.lib.stats.read.shared;

public class MinMaxMeanCountView extends DataView {

  public MinMaxMeanCountView(DataView other) {
    super(other);
  }

  public double getMinimum() {
    return super.minimum;
  }

  public double getMaximum() {
    return super.maximum;
  }

  public double getMean() {
    return super.mean;
  }

  public double getCount() {
    return super.count;
  }
}
