/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.world;

import jatcsimlib.global.KeyItem;

/**
 *
 * @author Marek
 */
public class Route implements KeyItem<String> {

  @Override
  public String getKey() {
    return name;
  }
  public enum eStarType{
    sid,
    star,
    transition
  }
  
  private eStarType type;
  private String name;
  private String route;
  private RunwayThreshold parent;

  public eStarType getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getRoute() {
    return route;
  }

  public RunwayThreshold getParent() {
    return parent;
  }

  public void setParent(RunwayThreshold parent) {
    this.parent = parent;
  }
  
}
