package eng.jAtcSim.lib.stats.read.shared;

public class MeanView extends DataView {

  public MeanView(DataView other) {
    super(other);
  }

  public double getMean(){
    return super.mean;
  }
}
