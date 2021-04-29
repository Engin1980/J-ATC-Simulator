package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class SnapshotStatsBlock implements IStatsBlock {
  private final int coveredSecondsCount;
  private final EDayTimeStamp startTime;
  private final SnapshotMMM airproxErrors;
  private final SnapshotMMM mrvaErrors;
  private final SnapshotMMM planeDelays;
  private final SnapshotMMM holdingPointCounts;
  private final SnapshotMMM holdingPointDelays;
  private final SnapshotMMM finishedDeparturesCount;
  private final SnapshotMMM finishedArrivalsCount;
  private final SnapshotMMM departuresCount;
  private final SnapshotMMM arrivalsCount;
  private final SnapshotMMM planesCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appDeparturesCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appArrivalsCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appTotalCount;

  public SnapshotStatsBlock(int coveredSecondsCount, EDayTimeStamp startTime, SnapshotMMM airproxErrors, SnapshotMMM mrvaErrors, SnapshotMMM planeDelays, SnapshotMMM holdingPointCounts, SnapshotMMM holdingPointDelays, SnapshotMMM finishedDeparturesCount, SnapshotMMM finishedArrivalsCount, SnapshotMMM departuresCount, SnapshotMMM arrivalsCount, SnapshotMMM planesCount, IReadOnlyMap<AtcId, SnapshotMMM> appDeparturesCount, IReadOnlyMap<AtcId, SnapshotMMM> appArrivalsCount, IReadOnlyMap<AtcId, SnapshotMMM> appTotalCount) {
    this.coveredSecondsCount = coveredSecondsCount;
    this.startTime = startTime;
    this.airproxErrors = airproxErrors;
    this.mrvaErrors = mrvaErrors;
    this.planeDelays = planeDelays;
    this.holdingPointCounts = holdingPointCounts;
    this.holdingPointDelays = holdingPointDelays;
    this.finishedDeparturesCount = finishedDeparturesCount;
    this.finishedArrivalsCount = finishedArrivalsCount;
    this.departuresCount = departuresCount;
    this.arrivalsCount = arrivalsCount;
    this.planesCount = planesCount;
    this.appDeparturesCount = appDeparturesCount;
    this.appArrivalsCount = appArrivalsCount;
    this.appTotalCount = appTotalCount;
  }

  @Override
  public IMMM getAirproxErrors() {
    return airproxErrors;
  }

  @Override
  public IReadOnlyMap<AtcId, ? extends IMMM> getAppArrivalsCount() {
    return appArrivalsCount;
  }

  @Override
  public IReadOnlyMap<AtcId, ? extends IMMM> getAppDeparturesCount() {
    return appDeparturesCount;
  }

  @Override
  public IReadOnlyMap<AtcId, ? extends IMMM> getAppPlanesCount() {
    return appTotalCount;
  }

  @Override
  public IMMM getArrivalsCount() {
    return arrivalsCount;
  }

  @Override
  public int getCoveredSecondsCount() {
    return coveredSecondsCount;
  }

  @Override
  public IMMM getDeparturesCount() {
    return departuresCount;
  }

  @Override
  public IMMM getFinishedArrivalsCount() {
    return finishedArrivalsCount;
  }

  @Override
  public IMMM getFinishedDeparturesCount() {
    return finishedDeparturesCount;
  }

  @Override
  public IMMM getHoldingPointCounts() {
    return holdingPointCounts;
  }

  @Override
  public IMMM getHoldingPointDelays() {
    return holdingPointDelays;
  }

  @Override
  public IMMM getMrvaErrors() {
    return mrvaErrors;
  }

  @Override
  public IMMM getFinishedArrivalDelays() {
    return planeDelays;
  }

  @Override
  public IMMM getPlanesCount() {
    return planesCount;
  }

  @Override
  public EDayTimeStamp getStartTime() {
    return startTime;
  }
}
