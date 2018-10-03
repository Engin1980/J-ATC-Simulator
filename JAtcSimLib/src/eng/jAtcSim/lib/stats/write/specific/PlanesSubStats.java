package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.Record;

public class PlanesSubStats {
    private Record departures = new Record();
    private Record arrivals = new Record();

  public Record getDepartures() {
    return departures;
  }

  public Record getArrivals() {
    return arrivals;
  }

  public Record getByType(boolean isArrival) {
      return isArrival ? this.arrivals : this.departures;
    }
}
