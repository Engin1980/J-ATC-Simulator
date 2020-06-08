package eng.jAtcSim.newLib.area.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.newStats.model.ElapsedSecondDurationModel;
import eng.jAtcSim.newLib.area.newStats.properties.TimedValue;

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

  public class Delays {
    public double getMean() {
      return RecentStats.this.planeDelays.mean(q -> (double) q.getValue());
    }

    public double getMaximum() {
      return RecentStats.this.planeDelays.maxInt(q -> q.getValue(), 0);
    }
  }

  public class HoldingPoint {
    public int getCount() {
      return RecentStats.this.holdingPointCurrentCount;
    }

    public int getMaximum() {
      return RecentStats.this.holdingPointMaximalCount.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximumDelay() {
      return RecentStats.this.holdingPointDelays.maxInt(q -> q.getValue(), 0);
    }

    public int getAverageDelay() {
      return (int) RecentStats.this.holdingPointDelays.mean(q -> (double) q.getValue());
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

    public int getArrivalsUnderApp() {
      return currentArrivalsUnderApp;
    }

    public int getDeparturesUnderApp() {
      return currentDeparturesUnderApp;
    }

    public int getArrivals() {
      return currentArrivals;
    }

    public int getDepartures() {
      return currentDepartures;
    }

    public int getMaximalArrivalsUnderApp() {
      return maximumArrivalsUnderApp.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalDeparturesUnderApp() {
      return maximumDeparturesUnderApp.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalUnderApp() {
      return maximumPlanesUnderApp.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalArrivals() {
      return maximumArrivals.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximalDepartures() {
      return maximumDepartures.maxInt(q -> q.getValue(), 0);
    }

    public int getMaximal() {
      return maximumPlanes.maxInt(q -> q.getValue(), 0);
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
  private int recentSecondsElapsed;
  private ElapsedSecondDurationModel elapsedSecondDuration = new ElapsedSecondDurationModel();
  @XmlIgnore
  private Errors clsErrors = new Errors();
  @XmlIgnore
  private Delays clsDelays = new Delays();
  @XmlIgnore
  private HoldingPoint clsHP = new HoldingPoint();
  @XmlIgnore
  private MovementsPerHour clsMovements = new MovementsPerHour();
  @XmlIgnore
  private CurrentPlanesCount clsCurrent = new CurrentPlanesCount();
  @XmlIgnore
  private FinishedPlanes clsFinished = new FinishedPlanes();
  private IList<TimedValue<Integer>> airproxErrors = new EList<>();
  private IList<TimedValue<Integer>> mrvaErrors = new EList();
  private IList<TimedValue<Integer>> planeDelays = new EList<>();
  private IList<TimedValue<Integer>> holdingPointMaximalCount = new EList<>();
  private int holdingPointCurrentCount;
  private IList<TimedValue<Integer>> holdingPointDelays = new EList<>();
  private IList<ETime> numberOfDepartures = new EList<>();
  private IList<ETime> numberOfLandings = new EList<>();
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

  public void elapseSecond() {
    if (recentSecondsElapsed < RECENT_INTERVAL_IN_SECONDS)
      recentSecondsElapsed++;

    ETime nowTime = Acc.now().clone();
    IReadOnlyList<Airplane> planes = Acc.prm().getPlanes();
    int airproxErs = 0;
    int mrvaErs = 0;
    int arrs = 0;
    int deps = 0;
    int aarrs = 0;
    int adeps = 0;
    for (Airplane plane : planes) {
      if (plane.getMrvaAirproxModule().getAirprox() == AirproxType.full)
        airproxErs++;
      if (plane.getMrvaAirproxModule().isMrvaError())
        mrvaErs++;

      boolean isApp = Acc.prm().getResponsibleAtc(plane) == Acc.atcApp();
      if (plane.getFlightModule().isArrival()) {
        arrs++;
        if (isApp) aarrs++;
      } else {
        deps++;
        if (isApp) adeps++;
      }
    }
    this.airproxErrors.add(new TimedValue<>(nowTime, airproxErs));
    this.mrvaErrors.add(new TimedValue<>(nowTime, mrvaErs));

    int hpCount = Acc.atcTwr().getNumberOfPlanesAtHoldingPoint();
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

    ETime lastTime = Acc.now().addHours(-1);
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
    this.numberOfDepartures.remove(q->q.isBefore(lastTime));
    this.numberOfLandings.remove(q->q.isBefore(lastTime));
  }

  public void registerElapsedSecondDuration(int ms) {
    elapsedSecondDuration.add(ms);
  }

  private <T> void cleanTimedList(IList<TimedValue<T>> lst, ETime lastTime) {
    lst.remove(q->q.getTime().isBefore(lastTime));
  }

  public void registerNewArrivalOrDeparture(boolean isArrival) {
    if (isArrival)
      this.numberOfLandings.add(Acc.now().clone());
    else
      this.numberOfDepartures.add(Acc.now().clone());
  }

  public void registerFinishedPlane(Airplane plane) {
    if (plane.getFlightModule().isArrival())
      this.finishedArrivals++;
    else
      this.finishedDepartures++;
    planeDelays.add(new TimedValue<>(Acc.now().clone(), plane.getFlightModule().getDelayDifference()));
  }

  public void registerHoldingPointDelay(int delay) {
    holdingPointDelays.add(new TimedValue<>(Acc.now().clone(), delay));
  }

  public ElapsedSecondDurationModel getElapsedSecondDuration() {
    return elapsedSecondDuration;
  }

  public HoldingPoint getHoldingPoint() {
    return clsHP;
  }

  public Errors getErrors() {
    return clsErrors;
  }

  public Delays getDelays() {
    return clsDelays;
  }

  public MovementsPerHour getMovementsPerHour() {
    return clsMovements;
  }

  public CurrentPlanesCount getCurrentPlanesCount() {
    return clsCurrent;
  }

  public FinishedPlanes getFinishedPlanes() {
    return clsFinished;
  }
}
