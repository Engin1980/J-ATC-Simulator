package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.Record;

public class ErrorsStats {
  private Record airproxes = new Record();
  private Record mrvas = new Record();

  public Record getAirproxes() {
    return airproxes;
  }

  public Record getMrvas() {
    return mrvas;
  }
}
