/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimdraw.painting;

import jatcsimlib.coordinates.Coordinate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class RadarEventManager {

  private final Radar parent;
  private final List<RadarEventListener> lst = new ArrayList();

  public RadarEventManager(Radar parent) {
    this.parent = parent;
  }

  public void addListener(RadarEventListener listener) {
    lst.add(listener);
  }

  public void removeListener(RadarEventListener listener) {
    if (lst.contains(listener)) {
      lst.remove(listener);
    }
  }
  
  public void raise(Coordinate coordinate){
    for (RadarEventListener l : lst){
      l.raise(parent, coordinate);
    }
  }
}
