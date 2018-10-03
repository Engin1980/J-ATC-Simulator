package eng.jAtcSim.lib.stats.read.shared;

public class MinMaxMeanCountCurrentView extends MinMaxMeanCountView {

  public MinMaxMeanCountCurrentView(DataView duration) {
    super(duration);
  }

  public double getCurrent() {
    return super.current;
  }
}
