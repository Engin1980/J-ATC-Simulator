package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.SharedInstanceProvider;
import eng.jAtcSim.newLib.shared.time.EDayTimeRun;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
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
  private int recentSecondsElapsed;
  private ElapsedSecondDurationModel elapsedSecondDuration = new ElapsedSecondDurationModel();
  //@XmlIgnore
  private Errors clsErrors = new Errors();
  //@XmlIgnore
  private Delays clsDelays = new Delays();
  //@XmlIgnore
  private HoldingPoint clsHP = new HoldingPoint();
  //@XmlIgnore
  private MovementsPerHour clsMovements = new MovementsPerHour();
  //@XmlIgnore
  private CurrentPlanesCount clsCurrent = new CurrentPlanesCount();
  //@XmlIgnore
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

  private static EDayTimeRun getNow(){
    return SharedInstanceProvider.getNow();
  }

  public CurrentPlanesCount getCurrentPlanesCount() {
    return clsCurrent;
  }

  public Delays getDelays() {
    return clsDelays;
  }

  public ElapsedSecondDurationModel getElapsedSecondDuration() {
    return elapsedSecondDuration;
  }

  public Errors getErrors() {
    return clsErrors;
  }

  public FinishedPlanes getFinishedPlanes() {
    return clsFinished;
  }

  public HoldingPoint getHoldingPoint() {
    return clsHP;
  }

  public MovementsPerHour getMovementsPerHour() {
    return clsMovements;
  }

  public void registerElapsedSecondDuration(int ms) {
    elapsedSecondDuration.add(ms);
  }

  public void registerFinishedPlane(boolean isArrival, EDayTimeStamp dayTimeStamp, int delayDifference){
    if (isArrival)
      this.finishedArrivals++;
    else
      this.finishedDepartures++;
    planeDelays.add(new TimedValue<>(dayTimeStamp, delayDifference));
  }

  public void registerHoldingPointDelay(EDayTimeStamp dayTimeStamp, int delay) {
    // get dayTimeStamp from Acc.now().clone()
    holdingPointDelays.add(new TimedValue<>(dayTimeStamp, delay));
  }

  public void registerNewArrivalOrDeparture(boolean isArrival, EDayTimeStamp dayTimeStamp) {
    if (isArrival)
      this.numberOfLandings.add(dayTimeStamp);
    else
      this.numberOfDepartures.add(dayTimeStamp);
  }

  private <T> void cleanTimedList(IList<TimedValue<T>> lst, EDayTimeStamp lastTime) {
    lst.remove(q -> q.getTime().isBefore(lastTime));
  }
}
