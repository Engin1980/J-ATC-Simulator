/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.atcs;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.shared.time.ETime;

/**
 *
 * @author Marek
 */
public class TimePlaneList  {
  private IList<TimeItem> inner = new EList<>();
  
  public void add (ETime time, Airplane plane){
    inner.add(new TimeItem(plane, time)); 
  }
}

class TimeItem{
  public final Airplane plane;
  public final ETime time;

  public TimeItem(Airplane plane, ETime time) {
    this.plane = plane;
    this.time = time;
  }
}
