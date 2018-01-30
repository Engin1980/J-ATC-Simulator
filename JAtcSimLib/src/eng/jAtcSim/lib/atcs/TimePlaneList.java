/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.atcs;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.ETime;
import jatcsimlib.airplanes.Airplane;
import jatcsimlib.global.ETime;
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