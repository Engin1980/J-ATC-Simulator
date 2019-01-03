package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.DataRecord;

public class ErrorsStats {
  private DataRecord airproxes = new DataRecord();
  private DataRecord mrvas = new DataRecord();

  public DataRecord getAirproxes() {
    return airproxes;
  }

  public DataRecord getMrvas() {
    return mrvas;
  }
}
