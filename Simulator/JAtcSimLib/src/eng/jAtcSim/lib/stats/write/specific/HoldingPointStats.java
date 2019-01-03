package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.DataRecord;

public class HoldingPointStats {
  private DataRecord delay = new DataRecord();
  private DataRecord count = new DataRecord();

  public DataRecord getDelay() {
    return delay;
  }

  public DataRecord getCount() {
    return count;
  }
}
