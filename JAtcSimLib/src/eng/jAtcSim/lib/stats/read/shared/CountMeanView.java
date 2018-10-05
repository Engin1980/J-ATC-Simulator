package eng.jAtcSim.lib.stats.read.shared;

public class CountMeanView extends MeanView {

  public CountMeanView(DataView other) {
    super(other);
  }

  public int getCount() {
    return (int) super.count;
  }
}
