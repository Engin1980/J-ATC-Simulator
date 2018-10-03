package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.CountMeanView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;

public class PlaneStats {
  private PlaneSubStats<MinMaxMeanCountCurrentView> planesInSim ;
  private PlaneSubStats<MinMaxMeanCountCurrentView> planesUnderApp ;
  private PlaneSubStats<CountMeanView> finishedPlanes ;
  private PlaneSubStats<MinMaxMeanCountCurrentView> delay ;

  public PlaneStats(PlaneSubStats<MinMaxMeanCountCurrentView> planesInSim, PlaneSubStats<MinMaxMeanCountCurrentView> planesUnderApp, PlaneSubStats<CountMeanView> finishedPlanes, PlaneSubStats<MinMaxMeanCountCurrentView> delay) {
    this.planesInSim = planesInSim;
    this.planesUnderApp = planesUnderApp;
    this.finishedPlanes = finishedPlanes;
    this.delay = delay;
  }

  public PlaneSubStats<MinMaxMeanCountCurrentView> getPlanesInSim() {
    return planesInSim;
  }

  public PlaneSubStats<MinMaxMeanCountCurrentView> getPlanesUnderApp() {
    return planesUnderApp;
  }

  public PlaneSubStats<CountMeanView> getFinishedPlanes() {
    return finishedPlanes;
  }

  public PlaneSubStats<MinMaxMeanCountCurrentView> getDelay() {
    return delay;
  }
}