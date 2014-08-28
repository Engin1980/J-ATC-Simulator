/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.KeyItem;
import jatcsimlib.global.KeyList;

/**
 *
 * @author Marek
 */
public class RunwayThreshold implements KeyItem<String> {
  private String name;
  private Coordinate coordinate;
  private final KeyList<Approach, Approach.eType> approaches = new KeyList();
  private final KeyList<Route, String> routes = new KeyList();
  private Runway parent;

  public String getName() {
    return name;
  }

  public Coordinate getCoordinate() {
    return coordinate;
  }

  public KeyList<Approach, Approach.eType> getApproaches() {
    return approaches;
  }

  public KeyList<Route, String> getRoutes() {
    return routes;
  }
  
  @Override
  public String getKey() {
    return getName();
  }

  public Runway getParent() {
    return parent;
  }

  public void setParent(Runway parent) {
    this.parent = parent;
  }
  
}
