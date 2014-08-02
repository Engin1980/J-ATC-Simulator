/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.world;

import jatcsimlib.types.KeyList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Area {

  private final KeyList<Airport, String> airports = new KeyList();
  private final KeyList<Navaid, String> navaids = new KeyList();
  private final List<Border> borders = new ArrayList();
  
  public KeyList<Airport, String> getAirports() {
    return airports;
  }

  public KeyList<Navaid, String> getNavaids() {
    return navaids;
  }

  public List<Border> getBorders() {
    return borders;
  }
  

}
