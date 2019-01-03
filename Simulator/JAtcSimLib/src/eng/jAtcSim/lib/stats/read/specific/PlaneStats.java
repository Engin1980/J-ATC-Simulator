package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.CountMeanView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;

public class PlaneStats {
  private PlanesSubStats<MinMaxMeanCountCurrentView> planesInSim ;
  private PlanesSubStats<MinMaxMeanCountCurrentView> planesUnderApp ;
  private PlanesSubStats<CountMeanView> finishedPlanes ;
  private PlanesSubStats<MinMaxMeanCountCurrentView> delay ;

  public PlaneStats(PlanesSubStats<MinMaxMeanCountCurrentView> planesInSim, PlanesSubStats<MinMaxMeanCountCurrentView> planesUnderApp, PlanesSubStats<CountMeanView> finishedPlanes, PlanesSubStats<MinMaxMeanCountCurrentView> delay) {
    this.planesInSim = planesInSim;
    this.planesUnderApp = planesUnderApp;
    this.finishedPlanes = finishedPlanes;
    this.delay = delay;
  }

  public PlanesSubStats<MinMaxMeanCountCurrentView> getPlanesInSim() {
    return planesInSim;
  }

  public PlanesSubStats<MinMaxMeanCountCurrentView> getPlanesUnderApp() {
    return planesUnderApp;
  }

  public PlanesSubStats<CountMeanView> getFinishedPlanes() {
    return finishedPlanes;
  }

  public PlanesSubStats<MinMaxMeanCountCurrentView> getDelay() {
    return delay;
  }
}