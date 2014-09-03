/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.global.KeyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Area {

  private String icao;
  private final KeyList<Airport, String> airports = new KeyList();
  private final KeyList<Navaid, String> navaids = new KeyList();
  private final List<Border> borders = new ArrayList();
  
  public KeyList<Airport, String> getAirports() {
    return airports;
  }

  public String getIcao() {
    return icao;
  }
  
  public KeyList<Navaid, String> getNavaids() {
    return navaids;
  }

  public List<Border> getBorders() {
    return borders;
  }
  
  public void rebuildParentReferences() {
    for (Airport a : this.getAirports()) {
      a.setParent(this);

      for (Runway r : a.getRunways()) {
        r.setParent(a);

        for (RunwayThreshold t : r.getThresholds()) {
          t.setParent(r);

          for (Route o : t.getRoutes()) {
            o.setParent(t);
          }
          for (Approach p : t.getApproaches()) {
            p.setParent(t);
          }
        }
      }
    }
  }
}
