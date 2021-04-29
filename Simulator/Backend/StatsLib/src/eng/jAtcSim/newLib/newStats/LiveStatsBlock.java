package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.contextLocal.Context;
import eng.jAtcSim.newLib.stats.properties.TimedValue;

public class LiveStatsBlock implements IStatsBlock {
  private final ElapsedSecondCounter elapsedSecondCounter = new ElapsedSecondCounter();
  private final EDayTimeStamp startTime;
  private final LiveMMM airproxErrors = new LiveMMM();
  private final LiveMMM mrvaErrors = new LiveMMM();
  private final LiveMMM planeDelays = new LiveMMM();
  private final LiveMMM holdingPointCounts = new LiveMMM();
  private final LiveMMM holdingPointDelays = new LiveMMM();
  private final LiveMMM finishedDeparturesCount = new LiveMMM();
  private final LiveMMM finishedArrivalsCount = new LiveMMM();
  private final LiveMMM departuresCount = new LiveMMM();
  private final LiveMMM arrivalsCount = new LiveMMM();
  private final LiveMMM planesCount = new LiveMMM();
  private final IMap<AtcId, LiveMMM> appDeparturesCount = new EMap<>();
  private final IMap<AtcId, LiveMMM> appArrivalsCount = new EMap<>();
  private final IMap<AtcId, LiveMMM> appPlanesCount = new EMap<>();

  public LiveStatsBlock(EDayTimeStamp startTime) {
    this.startTime = startTime;
    LiveMMM.setElapsedSecondCounter(elapsedSecondCounter);
  }

  public void elapseSecond(AnalysedPlanes analysedPlanes) {
    elapsedSecondCounter.inc();

    this.airproxErrors.addValue(analysedPlanes.airproxErrors);
    this.mrvaErrors.addValue(analysedPlanes.mrvaErrors);
    this.holdingPointCounts.addValue(analysedPlanes.planesAtHoldingPoint);
    this.arrivalsCount.addValue(analysedPlanes.arrivals);
    this.departuresCount.addValue(analysedPlanes.departures);
    this.planesCount.addValue(analysedPlanes.arrivals + analysedPlanes.departures);

    for (AtcId atcId : analysedPlanes.appArrivals.getKeys()) {
      this.appArrivalsCount.getOrSet(atcId, LiveMMM::new).addValue(analysedPlanes.appArrivals.get(atcId));
      this.appDeparturesCount.getOrSet(atcId, LiveMMM::new).addValue(analysedPlanes.appDepartures.get(atcId));
      this.appPlanesCount.getOrSet(atcId, LiveMMM::new)
              .addValue(analysedPlanes.appArrivals.get(atcId) + analysedPlanes.appDepartures.get(atcId));
    }
  }

  @Override
  public LiveMMM getAirproxErrors() {
    return airproxErrors;
  }

  @Override
  public IReadOnlyMap<AtcId, LiveMMM> getAppArrivalsCount() {
    return appArrivalsCount;
  }

  @Override
  public IReadOnlyMap<AtcId, LiveMMM> getAppDeparturesCount() {
    return appDeparturesCount;
  }

  @Override
  public IReadOnlyMap<AtcId, LiveMMM> getAppPlanesCount() {
    return appPlanesCount;
  }

  @Override
  public LiveMMM getArrivalsCount() {
    return arrivalsCount;
  }

  @Override
  public int getCoveredSecondsCount() {
    return elapsedSecondCounter.get();
  }

  @Override
  public LiveMMM getDeparturesCount() {
    return departuresCount;
  }

  @Override
  public LiveMMM getFinishedArrivalsCount() {
    return finishedArrivalsCount;
  }

  @Override
  public LiveMMM getFinishedDeparturesCount() {
    return finishedDeparturesCount;
  }

  @Override
  public LiveMMM getHoldingPointCounts() {
    return holdingPointCounts;
  }

  @Override
  public LiveMMM getHoldingPointDelays() {
    return holdingPointDelays;
  }

  @Override
  public LiveMMM getMrvaErrors() {
    return mrvaErrors;
  }

  @Override
  public LiveMMM getFinishedArrivalDelays() {
    return planeDelays;
  }

  @Override
  public LiveMMM getPlanesCount() {
    return planesCount;
  }

  @Override
  public EDayTimeStamp getStartTime() {
    return startTime;
  }
}
