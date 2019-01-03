package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.MeanView;

public class ErrorsStats {
  private MeanView airproxes;
  private MeanView mrvas;

  public ErrorsStats(MeanView airproxes, MeanView mrvas) {
    this.airproxes = airproxes;
    this.mrvas = mrvas;
  }

  public MeanView getAirproxes() {
    return airproxes;
  }

  public MeanView getMrvas() {
    return mrvas;
  }
}
