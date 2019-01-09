package eng.jAtcSim.lib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.newStats.model.ElapsedSecondDurationModel;
import eng.jAtcSim.lib.newStats.properties.TimedValue;

public class RecentStats {

  public class Errors {
    public double getAirproxErrorsPromile() {
      double sum = airproxErrors.sumInt(q -> q.getValue());
      double ret = sum / secondsElapsed;
      return ret;
    }

    public double getMrvaErrorsPromile() {
      double sum = mrvaErrors.sumInt(q -> q.getValue());
      double ret = sum / secondsElapsed;
      return ret;
    }
  }

  public class Delays {
    public double getMean() {
      return RecentStats.this.planeDelays.mean(q -> (double) q.getValue());
    }

    public double getMaximum() {
      return RecentStats.this.planeDelays.maxInt(q -> q.getValue());
    }
  }

  public class HoldingPoint {
    public int getCount() {
      return RecentStats.this.holdingPointCurrentCount;
    }

    public int getMaximum() {
      return RecentStats.this.holdingPointMaximalCount.maxInt(q -> q.getValue());
    }

    public int getMaximumDelay() {
      return RecentStats.this.holdingPointDelays.maxInt(q -> q.getValue());
    }

    public int getAverageDelay() {
      return (int) RecentStats.this.holdingPointDelays.mean(q -> (double) q.getValue());
    }

  }

  public class MovementsPerHour {
    public int getArrivals() {
      if (secondsElapsed == RECENT_INTERVAL_IN_SECONDS)
        return numberOfLandings.size();
      else
        return (int) (numberOfLandings.size() / (double) secondsElapsed * RECENT_INTERVAL_IN_SECONDS);
    }

    public int getDepartures() {
      if (secondsElapsed == RECENT_INTERVAL_IN_SECONDS)
        return numberOfDepartures.size();
      else
        return (int) (numberOfDepartures.size() / (double) secondsElapsed * RECENT_INTERVAL_IN_SECONDS);
    }
  }

  public class CurrentPlanesCount{

    public int getArrivalsUnderApp() {
      return currentArrivalsUnderApp;
    }
    public int getDeparturesUnderApp(){
      return currentDeparturesUnderApp;
    }
    public int getArrivals(){
      return currentArrivals;
    }
    public int getDepartures(){
      return currentDepartures;
    }
    public int getMaximalArrivalsUnderApp(){
      return maximumArrivalsUnderApp.maxInt(q->q.getValue());
    }
    public int getMaximalDeparturesUnderApp(){
      return maximumDeparturesUnderApp.maxInt(q->q.getValue());
    }
    public int getMaximalUnderApp(){
      return maximumPlanesUnderApp.maxInt(q->q.getValue());
    }
    public int getMaximalArrivals(){
      return maximumArrivals.maxInt(q->q.getValue());
    }
    public int getMaximalDepartures(){
      return maximumDepartures.maxInt(q->q.getValue());
    }
    public int getMaximal(){
      return maximumPlanes.maxInt(q->q.getValue());
    }
  }

  public class FinishedPlanes{
    public int getArrivals(){
      return finishedArrivals;
    }
    public int getDepartures(){
      return finishedDepartures;
    }
  }

  private static final int RECENT_INTERVAL_IN_SECONDS = 60;
  private int secondsElapsed;
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
  private IList<ETime> numberOfDepartures = new EList<>();
  private IList<ETime> numberOfLandings = new EList<>();
  private int currentArrivals;
  private int currentDepartures;
  private int currentArrivalsUnderApp;
  private int currentDeparturesUnderApp;
  private IList<TimedValue<Integer>> maximumArrivals= new EList<>();
  private IList<TimedValue<Integer>> maximumDepartures= new EList<>();
  private IList<TimedValue<Integer>> maximumPlanes= new EList<>();
  private IList<TimedValue<Integer>> maximumArrivalsUnderApp= new EList<>();
  private IList<TimedValue<Integer>> maximumDeparturesUnderApp= new EList<>();
  private IList<TimedValue<Integer>> maximumPlanesUnderApp= new EList<>();
  private int finishedArrivals;
  private int finishedDepartures;

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

  public MovementsPerHour getMovementsPerHour(){
    return clsMovements;
  }

  public CurrentPlanesCount getCurrentPlanesCount(){
    return clsCurrent;
  }

  public FinishedPlanes getFinishedPlanes(){
    return clsFinished;
  }
}
