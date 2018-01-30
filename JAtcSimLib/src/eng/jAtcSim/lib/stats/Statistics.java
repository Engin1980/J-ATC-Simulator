/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.stats;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.global.ETime;

/**
 *
 * @author Marek
 */
public class Statistics {

  public final Counter departures = new Counter();
  public final Counter arrivals = new Counter();

  private ETime startTime;

  public Statistics(ETime startTime) {
    if (startTime == null) {
      throw new IllegalArgumentException("Argument \"startTime\" cannot be null.");
    }
    
    this.startTime = startTime;
  }
  
  public double getAverageMovementsPerHour(ETime now) {
    double diff = now.getTotalSeconds() - startTime.getTotalSeconds();
    int mvmts = departures.get() + arrivals.get();

    double ret = mvmts / diff * 60 * 60;
    return ret;
  }

  public int getCountOfPlanesResponsibleFor(Atc.eType atcType) {
    int cnt = 0;
    for (Airplane p : Acc.planes()) {
      if (Acc.prm().getResponsibleAtc(p).getType() == atcType) {
        cnt++;
      }
    }
    return cnt;
  }
  
  public ETime getRunTime (ETime now){
    int sec = now.getTotalSeconds() - startTime.getTotalSeconds();
    ETime ret = new ETime(sec);
    return ret;
  }
  
  public int getCountOfPlanes(){
    return Acc.planes().size();
  }
}
