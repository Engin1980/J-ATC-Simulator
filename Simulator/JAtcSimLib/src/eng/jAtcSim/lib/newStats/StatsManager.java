package eng.jAtcSim.lib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.Validator;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirproxType;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.newStats.properties.CounterProperty;
import eng.jAtcSim.lib.newStats.properties.MMM;
import eng.jAtcSim.lib.newStats.properties.StatisticProperty;

public class StatsManager {
  private RecentStats recentStats = new RecentStats();
  private IList<Collector> collectors = new EList<>();
  private IList<MoodResult> moodResults = new EList<>();
  private IList<Snapshot> snapshots = new EList<>();
  private CounterProperty elapsedSecondsCounter = new CounterProperty();
  private int statsSnapshotDistanceInMinutes;

  public StatsManager(int statsSnapshotDistanceInMinutes) {
    Validator.check(statsSnapshotDistanceInMinutes > 1);
    this.statsSnapshotDistanceInMinutes = statsSnapshotDistanceInMinutes;
  }

  public IReadOnlyList<Snapshot> getSnapshots(boolean includeCurrentCollectors) {
    IList<Snapshot> ret;
    if (includeCurrentCollectors) {
      ret = new EList<>(snapshots);
      for (Collector collector : collectors) {
        Snapshot s = Snapshot.of(collector);
        ret.add(s);
      }
    } else
      ret = snapshots;

    return ret;
  }

  public void load(XElement root) {
    throw new UnsupportedOperationException("NOt implemented yet.");
  }

  public void init() {
    // nothing yet
  }

  public void save(XElement root) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }

  public void elapseSecond() {
    elapsedSecondsCounter.add();

    AnalysedPlanes tmp = analyseNumberOfPlanes();
    int hpPlanesCount = Acc.atcTwr().getNumberOfPlanesAtHoldingPoint();

    for (Collector collector : collectors) {
      collector.getPlanesInSim().getArrivals().add(tmp.arrivals);
      collector.getPlanesInSim().getDepartures().add(tmp.departures);
      collector.getPlanesInSim().getTotal().add(tmp.arrivals + tmp.departures);
      collector.getPlanesUnderApp().getArrivals().add(tmp.appArrivals);
      collector.getPlanesUnderApp().getDepartures().add(tmp.appDepartures);
      collector.getPlanesUnderApp().getTotal().add(tmp.appArrivals + tmp.appDepartures);
      collector.getErrors().getAirproxErros().add(tmp.airproxErrors);
      collector.getErrors().getMrvaErros().add(tmp.mrvaErrors);
      collector.adjustHoldingPointMaximumCount(hpPlanesCount);
    }
  }

  public void registerFinishedPlane(Airplane plane) {
    MoodResult mr = plane.getEvaluatedMood();
    this.moodResults.add(mr);
    for (Collector collector : collectors) {
      if (plane.isArrival()) {
        collector.getFinishedPlanesMoods().getArrivals().add(mr);
        if (!plane.isEmergency())
          collector.getFinishedPlanesDelays().getArrivals().add(plane.getDelayDifference());
      } else {
        collector.getFinishedPlanesMoods().getDepartures().add(mr);
        if (!plane.isEmergency())
          collector.getFinishedPlanesDelays().getDepartures().add(plane.getDelayDifference());
      }
    }
    recentStats.registerFinishedPlane(plane);
  }

  public void registerDeparture(int holdingPointSeconds) {
    for (Collector collector : collectors) {
      collector.getHoldingPointDelayStats().add(holdingPointSeconds);
      collector.getRunwayMovements().getDepartures().add();
    }
    recentStats.registerHoldingPointDelay(holdingPointSeconds);
    recentStats.registerNewArrivalOrDeparture(false);
  }

  public void registerArrival() {
    for (Collector collector : collectors) {
      collector.getRunwayMovements().getArrivals().add();
    }
    recentStats.registerNewArrivalOrDeparture(true);
  }

  public RecentStats getRecentStats() {
    return this.recentStats;
  }

  public void registerElapseSecondCalculationDuration(long ms) {
    for (Collector collector : collectors) {
      collector.getBusyCounter().add(ms);
    }
  }

  public int getElapsedSeconds() {
    return this.elapsedSecondsCounter.getCount();
  }

  /**
   * Analyses current number of planes in the simulation
   *
   * @return A complex data structure, where first index 0 = all planes, 1 = app planes, 2 = errors,
   * the second index 0 = arrivals, 1 = departures.
   * For errors, 0 = airproxes, 1 = mrvas
   */
  private AnalysedPlanes analyseNumberOfPlanes() {
    AnalysedPlanes ret = new AnalysedPlanes();
    for (Airplane plane : Acc.prm().getPlanes()) {
      boolean isApp = Acc.prm().getResponsibleAtc(plane) == Acc.atcApp();
      if (plane.isArrival()) {
        ret.arrivals++;
        if (isApp) ret.appArrivals++;
      } else {
        ret.departures++;
        if (isApp) ret.appDepartures++;
      }
      if (isApp) {
        if (plane.isMrvaError()) ret.mrvaErrors++;
        if (plane.getAirprox() == AirproxType.full) ret.airproxErrors++;
      }
    }
    return ret;
  }
}

class AnalysedPlanes {
  public int arrivals;
  public int departures;
  public int appArrivals;
  public int appDepartures;
  public int mrvaErrors;
  public int airproxErrors;
}