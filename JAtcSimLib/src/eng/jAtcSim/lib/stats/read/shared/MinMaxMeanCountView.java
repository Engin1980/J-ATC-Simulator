package eng.jAtcSim.lib.stats.read.shared;

public class MinMaxMeanCountView extends CountMeanView {

  public MinMaxMeanCountView(DataView other) {
    super(other);
  }

  public double getMinimum() {
    return super.minimum;
  }

  public double getMaximum() {
    return super.maximum;
  }
}
