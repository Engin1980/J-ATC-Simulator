/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.stats;

import eng.eSystem.utilites.CollectionUtils;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.AirproxType;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;

/**
 * @author Marek
 */
public class Statistics {

  public static class MaximumPlanes {
    public final Maxim arrivals = new Maxim();
    public final Maxim departures = new Maxim();
    public final Maxim total = new Maxim();
  }

  public class MovementsPerHour{
    public double getArrivals(){
      return finishedArrivals.get() / (double) secondsElapsed.get() * 3600;
    }
    public double getDepartures(){
      return finishedDepartures.get() / (double) secondsElapsed.get() * 3600;
    }
    public double getTotal(){
      return (finishedDepartures.get() + (double) finishedArrivals.get()) / secondsElapsed.get() * 3600;
    }
  }

  public static class HoldingPointInfo{
    public final Maxim maximumHoldingPointCount = new Maxim();
    public int currentHoldingPointCount = 0;
    public final Maxim maximumHoldingPointTime = new Maxim();
    public final Meaner meanHoldingPointTime = new Meaner();
  }

  public static class Delays{
    public final Maxim max = new Maxim();
    public final Meaner mean = new Meaner();

    public void add(int d) {
      max.set(d);
      mean.add(d);
    }
  }

  public static String toTime(double seconds){
    String ret;
    int tmp = (int) Math.floor(seconds);
    int hrs = tmp / 3600;
    tmp = tmp % 3600;
    int min = tmp / 60;
    tmp = tmp % 60;
    int sec = tmp;
    if (hrs == 0){
      ret = String.format("%d:%02d", min, sec);
    } else {
      ret = String.format("%d:%02d:%02d",hrs, min, sec);
    }
    return ret;
  }

  public static class CurrentPlanes {

    public int total;
    public int arrivals;
    public int departures;
    public int appTotal;
    public int appArrivals;
    public int appDepartures;

    public void update() {
      total = Acc.planes().size();
      arrivals = 0;
      departures = 0;
      appTotal = 0;
      appArrivals = 0;
      appDepartures = 0;
      for (Airplane airplane : Acc.planes()) {
        if (airplane.isArrival()) {
          arrivals++;
          if (airplane.getTunedAtc().getType() == Atc.eType.app) {
            appArrivals++;
            appTotal++;
          }
        } else {
          departures++;
          if (airplane.getTunedAtc().getType() == Atc.eType.app) {
            appDepartures++;
            appTotal++;
          }
        }
      }
    }
  }
  public final Counter finishedDepartures = new Counter();
  public final Counter finishedArrivals = new Counter();
  public final Counter secondsElapsed = new Counter();
  public final MaximumPlanes maximumResponsiblePlanes = new MaximumPlanes();
  public final MaximumPlanes maxumumTotalPlanes = new MaximumPlanes();
  public final Meaner durationOfSecondElapse = new Meaner();
  public final CurrentPlanes currentPlanes = new CurrentPlanes();
  public final MovementsPerHour movementsPerHour = new MovementsPerHour();
  public final HoldingPointInfo holdingPointInfo = new HoldingPointInfo();
  public final Meaner airproxes = new Meaner();
  public final Meaner mrvaErrors = new Meaner();
  public final Delays delays = new Delays();

  public Statistics() {
  }

  public double getAverageMovementsPerHour() {
    int mvmts = finishedDepartures.get() + finishedArrivals.get();

    double ret = mvmts / (double) secondsElapsed.get() * 60 * 60;
    return ret;
  }

  public void secondElapsed() {

    secondsElapsed.add();

    this.currentPlanes.update();

    this.maximumResponsiblePlanes.arrivals.set(this.currentPlanes.appArrivals);
    this.maximumResponsiblePlanes.departures.set(this.currentPlanes.appDepartures);
    this.maximumResponsiblePlanes.total.set(this.currentPlanes.appTotal);

    this.maxumumTotalPlanes.arrivals.set(this.currentPlanes.arrivals);
    this.maxumumTotalPlanes.departures.set(this.currentPlanes.departures);
    this.maxumumTotalPlanes.total.set(this.currentPlanes.total);

    int hpCount = Acc.atcTwr().getNumberOfPlanesAtHoldingPoint();
    this.holdingPointInfo.currentHoldingPointCount = hpCount;
    this.holdingPointInfo.maximumHoldingPointCount.set(hpCount);

    int tmp;

    tmp = CollectionUtils.where(Acc.planes(), q -> q.getAirprox()==AirproxType.full).size();
    airproxes.add(tmp);

    tmp = CollectionUtils.where(Acc.planes(), q -> q.isMrvaError()).size();
    mrvaErrors.add(tmp);
  }

  public ETime getRunTime() {
    ETime ret = new ETime(secondsElapsed.get());
    return ret;
  }

  public int getCountOfPlanes() {
    return Acc.planes().size();
  }
}

class StatisticHelper {

}
