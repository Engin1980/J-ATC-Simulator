package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.CountMeanView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;

public class PlanesCountStats {
  private PlanesViewBlock<MinMaxMeanCountCurrentView> planesInSim ;
  private PlanesViewBlock<MinMaxMeanCountCurrentView> planesUnderApp ;
  private PlanesViewBlock<CountMeanView> finishedPlanes ;
  private PlanesViewBlock<MinMaxMeanCountCurrentView> delay ;

  public PlanesCountStats(PlanesViewBlock<MinMaxMeanCountCurrentView> planesInSim, PlanesViewBlock<MinMaxMeanCountCurrentView> planesUnderApp, PlanesViewBlock<CountMeanView> finishedPlanes, PlanesViewBlock<MinMaxMeanCountCurrentView> delay) {
    this.planesInSim = planesInSim;
    this.planesUnderApp = planesUnderApp;
    this.finishedPlanes = finishedPlanes;
    this.delay = delay;
  }

  public PlanesViewBlock<MinMaxMeanCountCurrentView> getPlanesInSim() {
    return planesInSim;
  }

  public PlanesViewBlock<MinMaxMeanCountCurrentView> getPlanesUnderApp() {
    return planesUnderApp;
  }

  public PlanesViewBlock<CountMeanView> getFinishedPlanes() {
    return finishedPlanes;
  }

  public PlanesViewBlock<MinMaxMeanCountCurrentView> getDelay() {
    return delay;
  }
}