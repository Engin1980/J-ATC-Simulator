package eng.jAtcSim.newLib.stats.recent;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.SharedAcc;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.FinishedPlaneStats;
import eng.jAtcSim.newLib.stats.model.ElapsedSecondDurationModel;
import eng.jAtcSim.newLib.stats.properties.TimedValue;

public class RecentStats {

  public class Errors {
    public double getAirproxErrorsPromile() {
      double sum = airproxErrors.sumInt(q -> q.getValue());
      double ret = sum / recentSecondsElapsed;
      return ret;
    }

    public double getMrvaErrorsPromile() {
      double sum = mrvaErrors.sumInt(q -> q.getValue());
      double ret = sum / recentSecondsElapsed;
      return ret;
    }
  }

  //TODO rename to DelaysInfo
  public class Delays {
    public double getMaximum() {
      return RecentStats.this.planeDelays.maxInt(q -> q.getValue(), 0);
    }

    public double getMean() {
      return RecentStats.this.planeDelays.mean(q -> (double) q.getValue());
    }
  }

  public class HoldingPoint {
    public int getAverageDelay() {
      return (int) RecentStats.this.holdingPointDelays.mean(q -> (double) q.getValue());
    }

    public int getCount() {
      return RecentStats.this.holdingPointCurrentCount;
    }

    public int getMaximum() {
      return RecentStats.this.holdingPointMaximalCount.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximumDelay() {
      return RecentStats.this.holdingPointDelays.maxInt(q -> q.getValue(), 0);
    }

  }

  public class MovementsPerHour {
    public int getArrivals() {
      if (recentSecondsElapsed == RECENT_INTERVAL_IN_SECONDS)
        return numberOfLandings.size();
      else
        return (int) (numberOfLandings.size() / (double) recentSecondsElapsed * RECENT_INTERVAL_IN_SECONDS);
    }

    public int getDepartures() {
      if (recentSecondsElapsed == RECENT_INTERVAL_IN_SECONDS)
        return numberOfDepartures.size();
      else
        return (int) (numberOfDepartures.size() / (double) recentSecondsElapsed * RECENT_INTERVAL_IN_SECONDS);
    }
  }

  public class CurrentPlanesCount {

    public int getArrivals() {
      return currentArrivals;
    }

    public int getArrivalsUnderApp() {
      return currentArrivalsUnderApp;
    }

    public int getDepartures() {
      return currentDepartures;
    }

    public int getDeparturesUnderApp() {
      return currentDeparturesUnderApp;
    }

    public int getMaximal() {
      return maximumPlanes.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalArrivals() {
      return maximumArrivals.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalArrivalsUnderApp() {
      return maximumArrivalsUnderApp.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalDepartures() {
      return maximumDepartures.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalDeparturesUnderApp() {
      return maximumDeparturesUnderApp.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalUnderApp() {
      return maximumPlanesUnderApp.maxInt(q -> q.getValue(), 0);
    }
  }

  public class FinishedPlanes {
    public int getArrivals() {
      return finishedArrivals;
    }

    public int getDepartures() {
      return finishedDepartures;
    }
  }

  private static final int RECENT_INTERVAL_IN_SECONDS = 60;

  private static EDayTimeRun getNow() {
    return SharedAcc.getNow();
  }
  private int recentSecondsElapsed;
  private ElapsedSecondDurationModel elapsedSecondDuration = new ElapsedSecondDurationModel();
  private Errors clsErrors = new Errors();
  private Delays clsDelays = new Delays();
  private HoldingPoint clsHP = new HoldingPoint();
  private MovementsPerHour clsMovements = new MovementsPerHour();
  private CurrentPlanesCount clsCurrent = new CurrentPlanesCount();
  private FinishedPlanes clsFinished = new FinishedPlanes();
  private IList<TimedValue<Integer>> airproxErrors = new EList<>();
  private IList<TimedValue<Integer>> mrvaErrors = new EList();
  private IList<TimedValue<Integer>> planeDelays = new EList<>();
  private IList<TimedValue<Integer>> holdingPointMaximalCount = new EList<>();
  private int holdingPointCurrentCount;
  private IList<TimedValue<Integer>> holdingPointDelays = new EList<>();
  private IList<EDayTimeStamp> numberOfDepartures = new EList<>();
  private IList<EDayTimeStamp> numberOfLandings = new EList<>();
  private int currentArrivals;
  private int currentDepartures;
  private int currentArrivalsUnderApp;
  private int currentDeparturesUnderApp;
  private IList<TimedValue<Integer>> maximumArrivals = new EList<>();
  private IList<TimedValue<Integer>> maximumDepartures = new EList<>();
  private IList<TimedValue<Integer>> maximumPlanes = new EList<>();
  private IList<TimedValue<Integer>> maximumArrivalsUnderApp = new EList<>();
  private IList<TimedValue<Integer>> maximumDeparturesUnderApp = new EList<>();
  private IList<TimedValue<Integer>> maximumPlanesUnderApp = new EList<>();
  private int finishedArrivals;
  private int finishedDepartures;

  public void elapseSecond(AnalysedPlanes analysedPlanes) {
    if (recentSecondsElapsed < RECENT_INTERVAL_IN_SECONDS)
      recentSecondsElapsed++;

    EDayTimeStamp nowTime = getNow().toStamp();
    int airproxErs = analysedPlanes.airproxErrors;
    int mrvaErs = analysedPlanes.mrvaErrors;
    int arrs = analysedPlanes.arrivals;
    int deps = analysedPlanes.departures;
    int aarrs = analysedPlanes.appArrivals;
    int adeps = analysedPlanes.appDepartures;

    this.airproxErrors.add(new TimedValue<>(nowTime, airproxErs));
    this.mrvaErrors.add(new TimedValue<>(nowTime, mrvaErs));

    int hpCount = analysedPlanes.planesAtHoldingPoint;
    if (hpCount != this.holdingPointCurrentCount)
      this.holdingPointMaximalCount.add(new TimedValue<>(nowTime, hpCount));
    this.holdingPointCurrentCount = hpCount;

    boolean upd = false;
    if (arrs != this.currentArrivals) {
      this.maximumArrivals.add(new TimedValue<>(nowTime, arrs));
      upd = true;
    }
    this.currentArrivals = arrs;
    if (deps != this.currentDepartures) {
      this.maximumDepartures.add(new TimedValue<>(nowTime, deps));
      upd = true;
    }
    this.currentDepartures = deps;
    if (upd) {
      this.maximumPlanes.add(new TimedValue<>(nowTime, arrs + deps));
      upd = false;
    }
    if (aarrs != this.currentArrivalsUnderApp) {
      this.maximumArrivalsUnderApp.add(new TimedValue<>(nowTime, aarrs));
      upd = true;
    }
    this.currentArrivalsUnderApp = aarrs;
    if (adeps != this.currentDeparturesUnderApp) {
      this.maximumDeparturesUnderApp.add(new TimedValue<>(nowTime, adeps));
      upd = true;
    }
    this.currentDeparturesUnderApp = adeps;
    if (upd)
      this.maximumPlanesUnderApp.add(new TimedValue<>(nowTime, aarrs + adeps));

    EDayTimeStamp lastTime = SharedAcc.getNow().addHours(-1);
    cleanTimedList(this.airproxErrors, lastTime);
    cleanTimedList(this.mrvaErrors, lastTime);
    cleanTimedList(this.planeDelays, lastTime);
    cleanTimedList(this.holdingPointMaximalCount, lastTime);
    cleanTimedList(this.holdingPointDelays, lastTime);
    cleanTimedList(this.maximumArrivals, lastTime);
    cleanTimedList(this.maximumDepartures, lastTime);
    cleanTimedList(this.maximumPlanes, lastTime);
    cleanTimedList(this.maximumArrivalsUnderApp, lastTime);
    cleanTimedList(this.maximumDeparturesUnderApp, lastTime);
    cleanTimedList(this.maximumPlanesUnderApp, lastTime);
    this.numberOfDepartures.remove(q -> q.isBefore(lastTime));
    this.numberOfLandings.remove(q -> q.isBefore(lastTime));
  }

  public void registerFinishedPlane(FinishedPlaneStats finishedPlaneStats) {
    if (finishedPlaneStats.isArrival())
      this.finishedArrivals++;
    else
      this.finishedDepartures++;
    planeDelays.add(new TimedValue<>(SharedAcc.getNow().toStamp(), finishedPlaneStats.getDelayDifference()));
  }

  //  public CurrentPlanesCount getCurrentPlanesCount() {
//    return clsCurrent;
//  }
//
//  public Delays getDelays() {
//    return clsDelays;
//  }
//
//  public ElapsedSecondDurationModel getElapsedSecondDuration() {
//    return elapsedSecondDuration;
//  }
//
//  public Errors getErrors() {
//    return clsErrors;
//  }
//
//  public FinishedPlanes getFinishedPlanes() {
//    return clsFinished;
//  }
//
//  public HoldingPoint getHoldingPoint() {
//    return clsHP;
//  }
//
//  public MovementsPerHour getMovementsPerHour() {
//    return clsMovements;
//  }
//
//  public void registerElapsedSecondDuration(int ms) {
//    elapsedSecondDuration.add(ms);
//  }
//

//
//  public void registerHoldingPointDelay(EDayTimeStamp dayTimeStamp, int delay) {
//    // get dayTimeStamp from Acc.now().clone()
//    holdingPointDelays.add(new TimedValue<>(dayTimeStamp, delay));
//  }
//
//  public void registerNewArrivalOrDeparture(boolean isArrival, EDayTimeStamp dayTimeStamp) {
//    if (isArrival)
//      this.numberOfLandings.add(dayTimeStamp);
//    else
//      this.numberOfDepartures.add(dayTimeStamp);
//  }

  private <T> void cleanTimedList(IList<TimedValue<T>> lst, EDayTimeStamp lastTime) {
    lst.remove(q -> q.getTime().isBefore(lastTime));
  }
}
