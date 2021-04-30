package eng.jAtcSim.newLib.newStats.blocks;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyMap;
import eng.jAtcSim.newLib.newStats.values.IMMM;
import eng.jAtcSim.newLib.newStats.values.LiveMMM;
import eng.jAtcSim.newLib.newStats.values.SnapshotMMM;
import eng.jAtcSim.newLib.shared.AtcId;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class SnapshotStatsBlock implements IStatsBlock {
  private final int coveredSecondsCount;
  private final EDayTimeStamp startTime;
  private final SnapshotMMM airproxErrors;
  private final SnapshotMMM mrvaErrors;
  private final SnapshotMMM finishedArrivalDelays;
  private final SnapshotMMM holdingPointCounts;
  private final SnapshotMMM holdingPointDelays;
  private final SnapshotMMM finishedDeparturesCount;
  private final SnapshotMMM finishedArrivalsCount;
  private final SnapshotMMM departuresCount;
  private final SnapshotMMM arrivalsCount;
  private final SnapshotMMM planesCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appDeparturesCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appArrivalsCount;
  private final IReadOnlyMap<AtcId, SnapshotMMM> appPlanesCount;
  private final SnapshotMMM finishedArrivalsMoodValues;
  private final SnapshotMMM finishedDeparturesMoodValues;

  public SnapshotStatsBlock(LiveStatsBlock b) {
    this.coveredSecondsCount = b.getCoveredSecondsCount();
    this.startTime = b.getStartTime();
    this.airproxErrors = toSnapshot(b.getAirproxErrors());
    this.mrvaErrors = toSnapshot(b.getMrvaErrors());
    this.planesCount = toSnapshot(b.getPlanesCount());
    this.finishedArrivalDelays = toSnapshot(b.getFinishedArrivalDelays());
    this.holdingPointCounts = toSnapshot(b.getHoldingPointCounts());
    this.holdingPointDelays = toSnapshot(b.getHoldingPointDelays());
    this.finishedDeparturesCount = toSnapshot(b.getFinishedDeparturesCount());
    this.finishedArrivalsCount = toSnapshot(b.getFinishedArrivalsCount());
    this.departuresCount = toSnapshot(b.getDeparturesCount());
    this.arrivalsCount = toSnapshot(b.getArrivalsCount());
    this.appDeparturesCount = toSnapshot(b.getAppDeparturesCount());
    this.appArrivalsCount = toSnapshot(b.getAppArrivalsCount());
    this.appPlanesCount = toSnapshot(b.getAppPlanesCount());
    this.finishedArrivalsMoodValues = toSnapshot(b.getFinishedArrivalsMoodValues());
    this.finishedDeparturesMoodValues = toSnapshot(b.getFinishedDeparturesMoodValues());
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
    return appPlanesCount;
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
  public IMMM getFinishedArrivalDelays() {
    return finishedArrivalDelays;
  }

  @Override
  public IMMM getFinishedArrivalsCount() {
    return finishedArrivalsCount;
  }

  @Override
  public IMMM getFinishedArrivalsMoodValues() {
    return finishedArrivalsMoodValues;
  }

  @Override
  public IMMM getFinishedDeparturesCount() {
    return finishedDeparturesCount;
  }

  @Override
  public IMMM getFinishedDeparturesMoodValues() {
    return finishedDeparturesMoodValues;
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
  public IMMM getPlanesCount() {
    return planesCount;
  }

  @Override
  public EDayTimeStamp getStartTime() {
    return startTime;
  }

  private IReadOnlyMap<AtcId, SnapshotMMM> toSnapshot(IReadOnlyMap<AtcId, LiveMMM> map) {
    IMap<AtcId, SnapshotMMM> ret = new EMap<>();
    map.forEach(q -> ret.set(q.getKey(), toSnapshot(q.getValue())));
    return ret;
  }

  private SnapshotMMM toSnapshot(LiveMMM mmm) {
    return new SnapshotMMM(mmm.getMinimum(), mmm.getMaximum(), mmm.getMean());
  }
}
