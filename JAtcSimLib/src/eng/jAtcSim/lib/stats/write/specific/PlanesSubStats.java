package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.DataRecord;

public class PlanesSubStats {
  private DataRecord departures = new DataRecord();
  private DataRecord arrivals = new DataRecord();
  private DataRecord together = new DataRecord();

  public DataRecord getDepartures() {
    return departures;
  }

  public DataRecord getArrivals() {
    return arrivals;
  }

  public DataRecord getTogether() {
    return together;
  }

  public DataRecord getByType(boolean isArrival) {
    return isArrival ? this.arrivals : this.departures;
  }
}
