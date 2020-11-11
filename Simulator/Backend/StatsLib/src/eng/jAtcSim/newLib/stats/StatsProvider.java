package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.ToDoException;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.contextLocal.Context;
import eng.jAtcSim.newLib.stats.properties.CounterProperty;
import eng.jAtcSim.newLib.stats.recent.RecentStats;

public class StatsProvider {

  public class MyStatsProvider implements IStatsProvider {

    @Override
    public int getElapsedSeconds() {
      return StatsProvider.this.elapsedSecondsCounter.getCount();
    }

    @Override
    public IReadOnlyList<MoodResult> getFullMoodHistory() {
      return StatsProvider.this.moodResults;
    }

    @Override
    public RecentStats getRecentStats() {
      return StatsProvider.this.recentStats;
    }

    @Override
    public IReadOnlyList<Snapshot> getSnapshots(int step) {
      //TODO Implement this: how?
      throw new ToDoException("how?");
    }
  }

  public static void prepareXmlContext(eng.newXmlUtils.XmlContext ctx) {
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.model");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.properties");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.recent");
    ctx.sdfManager.addAutomaticallySerializedPackage("eng.jAtcSim.newLib.stats.xml");
  }

  private final IList<Collector> collectors = new EList<>();
  private final CounterProperty elapsedSecondsCounter = new CounterProperty();
  private final IList<MoodResult> moodResults = new EList<>();
  private EDayTimeStamp nextCollectorStartTime = null;
  private final RecentStats recentStats = new RecentStats();
  private final IList<Snapshot> snapshots = new EList<>();
  private final int statsSnapshotDistanceInMinutes;
  private final MyStatsProvider myStatsProvider = this.new MyStatsProvider();

  public StatsProvider(int statsSnapshotDistanceInMinutes) {
    this.statsSnapshotDistanceInMinutes = statsSnapshotDistanceInMinutes;
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

  public IStatsProvider getPublicStats() {
    return this.myStatsProvider;
  }

  public void init() {
    // intentionally blank
  }

  public void registerArrival() {
    for (Collector collector : collectors) {
      collector.getRunwayMovements().getArrivals().add();
    }
    recentStats.registerNewArrivalOrDeparture(true);
  }

  public void registerDeparture(int holdingPointSeconds) {
    for (Collector collector : collectors) {
      collector.getHoldingPointDelayStats().add(holdingPointSeconds);
      collector.getRunwayMovements().getDepartures().add();
    }
    recentStats.registerHoldingPointDelay(holdingPointSeconds);
    recentStats.registerNewArrivalOrDeparture(false);
  }

  public void registerElapseSecondDuration(int ms) {
    for (Collector collector : collectors) {
      collector.getBusyCounter().add(ms);
    }
    recentStats.registerElapsedSecondDuration(ms);
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
    EDayTimeStamp now = Context.getShared().getNow().toStamp();
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
