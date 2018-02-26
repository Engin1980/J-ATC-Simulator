/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.stats;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;

/**
 * @author Marek
 */
public class Statistics {

  public final Counter finishedDepartures = new Counter();
  public final Counter finishedArrivals = new Counter();
  public final Counter secondsElapsed = new Counter();
  public final Planes responsiblePlanes = new Planes();
  public final Planes totalPlanes = new Planes();
  public final Meaner durationOfSecondElapse = new Meaner();

  static class Planes {
    public final Maxim total = new Maxim();
    public final Maxim arrivals = new Maxim();
    public final Maxim departures = new Maxim();
  }

  public Statistics() {
  }

  public double getAverageMovementsPerHour() {
    int mvmts = finishedDepartures.get() + finishedArrivals.get();

    double ret = mvmts / (double) secondsElapsed.get() * 60 * 60;
    return ret;
  }

  public void secondElapsed(){

    secondsElapsed.add();

    int arrs = StatisticHelper.getCountOfPlanesResponsibleFor(Atc.eType.app, true);
    int deps = StatisticHelper.getCountOfPlanesResponsibleFor(Atc.eType.app,  false );
    this.responsiblePlanes.arrivals.set(arrs);
    this.responsiblePlanes.departures.set(deps);
    this.responsiblePlanes.total.set(arrs+deps);

    arrs = StatisticHelper.getCountOfPlanes( true);
    deps = StatisticHelper.getCountOfPlanes(  false );
    this.totalPlanes.arrivals.set(arrs);
    this.totalPlanes.departures.set(deps);
    this.totalPlanes.total.set(arrs+deps);

  }

  public ETime getRunTime() {
    ETime ret = new ETime(secondsElapsed.get());
    return ret;
  }

  public int getCountOfPlanes() {
    return Acc.planes().size();
  }
}

class StatisticHelper{
  public static int getCountOfPlanesResponsibleFor(Atc.eType atcType, boolean arrivals) {
    int cnt = 0;
    for (Airplane p : Acc.planes()) {
      if (Acc.prm().getResponsibleAtc(p).getType() == atcType && p.isArrival() == arrivals) {
        cnt++;
      }
    }
    return cnt;
  }

  public static int getCountOfPlanes(boolean arrivals) {
    int cnt = 0;
    for (Airplane p : Acc.planes()) {
      if (p.isArrival() == arrivals) {
        cnt++;
      }
    }
    return cnt;
  }
}
