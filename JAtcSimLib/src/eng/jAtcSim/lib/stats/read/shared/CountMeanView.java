package eng.jAtcSim.lib.stats.read.shared;

public class CountMeanView extends DataView {

  public CountMeanView(DataView other) {
    super(other);
  }

  public int getCount() {
    return (int) super.count;
  }

  public double getMean() {
    return super.mean;
  }
}
