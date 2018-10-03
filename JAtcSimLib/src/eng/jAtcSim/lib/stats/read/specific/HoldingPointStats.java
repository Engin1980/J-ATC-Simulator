package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;

public class HoldingPointStats {
  private MinMaxMeanCountCurrentView delay ;
  private MinMaxMeanCountCurrentView count ;

  public HoldingPointStats(MinMaxMeanCountCurrentView delay, MinMaxMeanCountCurrentView count) {
    this.delay = delay;
    this.count = count;
  }

  public MinMaxMeanCountCurrentView getDelay() {
    return delay;
  }

  public MinMaxMeanCountCurrentView getCount() {
    return count;
  }
}
