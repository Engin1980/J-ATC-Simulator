//package eng.jAtcSim.newLib.stats;
//
//import eng.eSystem.collections.EList;
//import eng.eSystem.collections.IList;
//import eng.eSystem.collections.IReadOnlyList;
//import eng.eSystem.eXml.XElement;
//import eng.eSystem.validation.EAssert;
//import eng.eSystem.validation.Validator;
//import eng.jAtcSim.newLib.Acc;
//import eng.jAtcSim.newLib.area.airplanes.Airplane;
//import eng.jAtcSim.newLib.area.airplanes.AirproxType;
//import eng.jAtcSim.newLib.area.airplanes.moods.MoodResult;
//import eng.jAtcSim.newLib.area.serialization.LoadSave;
//import eng.jAtcSim.newLib.mood.MoodResult;
//import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
//import eng.jAtcSim.newLib.stats.properties.CounterProperty;
//
//public class StatsManager {
//  private RecentStats recentStats = new RecentStats();
//  private IList<Collector> collectors = new EList<>();
//  private IList<MoodResult> moodResults = new EList<>();
//  private IList<Snapshot> snapshots = new EList<>();
//  private CounterProperty elapsedSecondsCounter = new CounterProperty();
//  private int statsSnapshotDistanceInMinutes;
//  private EDayTimeStamp nextCollectorStartTime = null;
//
//
//  public StatsManager(int statsSnapshotDistanceInMinutes) {
//    EAssert.isTrue(statsSnapshotDistanceInMinutes > 1);
//    this.statsSnapshotDistanceInMinutes = statsSnapshotDistanceInMinutes;
//  }
//
//  public void elapseSecond() {
//    elapsedSecondsCounter.add();
//    recentStats.elapseSecond();
//
//    manageCollectors();
//
//    AnalysedPlanes tmp = analyseNumberOfPlanes();
//    int hpPlanesCount = Acc.atcTwr().getNumberOfPlanesAtHoldingPoint();
//
//    for (Collector collector : collectors) {
//      collector.getPlanesInSim().getArrivals().add(tmp.arrivals);
//      collector.getPlanesInSim().getDepartures().add(tmp.departures);
//      collector.getPlanesInSim().getTotal().add(tmp.arrivals + tmp.departures);
//      collector.getPlanesUnderApp().getArrivals().add(tmp.appArrivals);
//      collector.getPlanesUnderApp().getDepartures().add(tmp.appDepartures);
//      collector.getPlanesUnderApp().getTotal().add(tmp.appArrivals + tmp.appDepartures);
//      collector.getErrors().getAirproxErros().add(tmp.airproxErrors);
//      collector.getErrors().getMrvaErros().add(tmp.mrvaErrors);
//      collector.adjustHoldingPointMaximumCount(hpPlanesCount);
//    }
//  }
//
//  public int getElapsedSeconds() {
//    return this.elapsedSecondsCounter.getCount();
//  }
//
//  public IReadOnlyList<MoodResult> getFullMoodHistory() {
//    return this.moodResults;
//  }
//
//  public RecentStats getRecentStats() {
//    return this.recentStats;
//  }
//
//  public IReadOnlyList<Snapshot> getSnapshots(boolean includeCurrentCollectors) {
//    IList<Snapshot> ret;
//    if (includeCurrentCollectors) {
//      ret = new EList<>(snapshots);
//      for (Collector collector : collectors) {
//        Snapshot s = Snapshot.of(collector);
//        ret.add(s);
//      }
//    } else
//      ret = snapshots;
//
//    return ret;
//  }
//
//  public IReadOnlyList<Snapshot> getSnapshots(int mergeDistance) {
//    Validator.check(mergeDistance >= 1);
//
//    IList<Snapshot> ret = new EList<>();
//    int index = 0;
//    while (index < snapshots.size()) {
//      if (mergeDistance == 1) {
//        ret.add(this.snapshots.get(index));
//        index++;
//      } else {
//        int toIndex = index + mergeDistance;
//        IReadOnlyList<Snapshot> tmp = this.snapshots.get(index, toIndex);
//        Snapshot s = Snapshot.createMerge(tmp);
//        ret.add(s);
//        index = toIndex;
//      }
//
//    }
//
//    return ret;
//  }
//
//  public void init() {
//    // nothing yet
//  }
//
//  public void load(XElement root) {
//    LoadSave.loadField(root, this, "statsManager");
//  }
//
//  public void registerArrival() {
//    for (Collector collector : collectors) {
//      collector.getRunwayMovements().getArrivals().add();
//    }
//    recentStats.registerNewArrivalOrDeparture(true);
//  }
//
//  public void registerDeparture(int holdingPointSeconds) {
//    for (Collector collector : collectors) {
//      collector.getHoldingPointDelayStats().add(holdingPointSeconds);
//      collector.getRunwayMovements().getDepartures().add();
//    }
//    recentStats.registerHoldingPointDelay(holdingPointSeconds);
//    recentStats.registerNewArrivalOrDeparture(false);
//  }
//
//  public void registerElapseSecondCalculationDuration(int ms) {
//    for (Collector collector : collectors) {
//      collector.getBusyCounter().add(ms);
//    }
//    recentStats.registerElapsedSecondDuration(ms);
//  }
//
//  public void registerFinishedPlane(Airplane plane) {
//    MoodResult mr = plane.getEvaluatedMood();
//    this.moodResults.add(mr);
//    for (Collector collector : collectors) {
//      if (plane.getFlightModule().isArrival()) {
//        collector.getFinishedPlanesMoods().getArrivals().add(mr);
//        if (!plane.getEmergencyModule().isEmergency())
//          collector.getFinishedPlanesDelays().getArrivals().add(plane.getFlightModule().getDelayDifference());
//      } else {
//        collector.getFinishedPlanesMoods().getDepartures().add(mr);
//        if (!plane.getEmergencyModule().isEmergency())
//          collector.getFinishedPlanesDelays().getDepartures().add(plane.getFlightModule().getDelayDifference());
//      }
//    }
//    recentStats.registerFinishedPlane(plane);
//  }
//
//  public void save(XElement root) {
//    LoadSave.saveField(root, this, "statsManager");
//  }
//
//  /**
//   * Analyses current number of planes in the simulation
//   *
//   * @return A complex data structure, where first index 0 = all planes, 1 = app planes, 2 = errors,
//   * the second index 0 = arrivals, 1 = departures.
//   * For errors, 0 = airproxes, 1 = mrvas
//   */
//  private AnalysedPlanes analyseNumberOfPlanes() {
//    AnalysedPlanes ret = new AnalysedPlanes();
//    for (Airplane plane : Acc.prm().getPlanes()) {
//      boolean isApp = Acc.prm().getResponsibleAtc(plane) == Acc.atcApp();
//      if (plane.getFlightModule().isArrival()) {
//        ret.arrivals++;
//        if (isApp) ret.appArrivals++;
//      } else {
//        ret.departures++;
//        if (isApp) ret.appDepartures++;
//      }
//      if (isApp) {
//        if (plane.getMrvaAirproxModule().isMrvaError()) ret.mrvaErrors++;
//        if (plane.getMrvaAirproxModule().getAirprox() == AirproxType.full) ret.airproxErrors++;
//      }
//    }
//    return ret;
//  }
//
//  private void manageCollectors() {
//    if (nextCollectorStartTime == null) {
//      // initialization
//      Collector c = new Collector(Acc.now().clone(), Acc.now().addMinutes(statsSnapshotDistanceInMinutes * 2));
//      collectors.add(c);
//      nextCollectorStartTime = Acc.now().addMinutes(statsSnapshotDistanceInMinutes);
//    } else if (Acc.now().isAfterOrEq(this.nextCollectorStartTime)) {
//      Collector c = collectors.tryGetFirst(q -> q.getToTime().isBeforeOrEq(Acc.now()));
//      if (c != null) {
//        Snapshot s = Snapshot.of(c);
//        collectors.remove(c);
//        snapshots.add(s);
//      }
//      c = new Collector(Acc.now().clone(), Acc.now().addMinutes(statsSnapshotDistanceInMinutes * 2));
//      collectors.add(c);
//      nextCollectorStartTime = Acc.now().addMinutes(statsSnapshotDistanceInMinutes);
//    }
//  }
//}
//
//class AnalysedPlanes {
//  public int arrivals;
//  public int departures;
//  public int appArrivals;
//  public int appDepartures;
//  public int mrvaErrors;
//  public int airproxErrors;
//}
