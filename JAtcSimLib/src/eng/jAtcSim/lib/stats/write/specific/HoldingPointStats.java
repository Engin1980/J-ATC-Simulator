package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.Record;

public class HoldingPointStats {
  private Record delay = new Record();
  private Record count = new Record();

  public Record getDelay() {
    return delay;
  }

  public Record getCount() {
    return count;
  }
}
