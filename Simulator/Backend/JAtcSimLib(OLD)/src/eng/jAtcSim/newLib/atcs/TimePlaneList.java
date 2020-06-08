/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.atcs;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.global.ETime;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class TimePlaneList  {
  private List<TimeItem> inner = new LinkedList<>();
  
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
