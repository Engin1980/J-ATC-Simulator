package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.properties.CounterProperty;
import eng.jAtcSim.newLib.stats.recent.RecentStats;

public class StatsProvider {
  private final CounterProperty elapsedSecondsCounter = new CounterProperty();
  private final RecentStats recentStats = new RecentStats();
  private final IList<Collector> collectors = new EList<>();
  private EDayTimeStamp nextCollectorStartTime = null;
  private final IList<Snapshot> snapshots = new EList<>();
  private int statsSnapshotDistanceInMinutes;
  private final IList<MoodResult> moodResults = new EList<>();

  public StatsProvider(int statsSnapshotDistanceInMinutes) {
    throw new ToDoException();
  }

  public void elapseSecond(AnalysedPlanes analysedPlanes) {
    elapsedSecondsCounter.add();
    recentStats.elapseSecond(analysedPlanes);

    snapshotizeCollectors();

    for (Collector collector : collectors) {
      collector.getPlanesInSim().getArrivals().add(analysedPlanes.arrivals);
      collector.getPlanesInSim().getDepartures().add(analysedPlanes.departures);
      collector.getPlanesInSim().getTotal().add(analysedPlanes.arrivals + analysedPlanes.departures);
      collector.getPlanesUnderApp().getArrivals().add(analysedPlanes.appArrivals);
      collector.getPlanesUnderApp().getDepartures().add(analysedPlanes.appDepartures);
      collector.getPlanesUnderApp().getTotal().add(analysedPlanes.appArrivals + analysedPlanes.appDepartures);
      collector.getErrors().getAirproxErros().add(analysedPlanes.airproxErrors);
      collector.getErrors().getMrvaErros().add(analysedPlanes.mrvaErrors);
      collector.adjustHoldingPointMaximumCount(analysedPlanes.planesAtHoldingPoint);
    }
  }

  public void init() {

  }

  public void registerFinishedPlane(FinishedPlaneStats finishedPlaneStats) {
    MoodResult mr = finishedPlaneStats.getMoodResult();
    this.moodResults.add(mr);
    for (Collector collector : collectors) {
      if (finishedPlaneStats.isArrival()) {
        collector.getFinishedPlanesMoods().getArrivals().add(mr);
        if (!finishedPlaneStats.isEmergency())
          collector.getFinishedPlanesDelays().getArrivals().add(finishedPlaneStats.getDelayDifference());
      } else {
        collector.getFinishedPlanesMoods().getDepartures().add(mr);
        if (!finishedPlaneStats.isEmergency())
          collector.getFinishedPlanesDelays().getDepartures().add(finishedPlaneStats.getDelayDifference());
      }
    }
    recentStats.registerFinishedPlane(finishedPlaneStats);
  }

  private void snapshotizeCollectors() {
    EDayTimeStamp now = SharedAcc.getNow().toStamp();
    if (nextCollectorStartTime == null) {
      // initialization
      Collector c = new Collector(now, now.addMinutes(statsSnapshotDistanceInMinutes * 2));
      collectors.add(c);
      nextCollectorStartTime = now.addMinutes(statsSnapshotDistanceInMinutes);
    } else if (now.isAfterOrEq(this.nextCollectorStartTime)) {
      Collector c = collectors.tryGetFirst(q -> q.getToTime().isBeforeOrEq(now));
      if (c != null) {
        Snapshot s = Snapshot.of(c);
        collectors.remove(c);
        snapshots.add(s);
      }
      c = new Collector(now, now.addMinutes(statsSnapshotDistanceInMinutes * 2));
      collectors.add(c);
      nextCollectorStartTime = now.addMinutes(statsSnapshotDistanceInMinutes);
    }
  }
}
