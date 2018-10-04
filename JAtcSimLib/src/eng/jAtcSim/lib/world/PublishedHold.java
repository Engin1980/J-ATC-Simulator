/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.eSystem.xmlSerialization.annotations.XmlOptional;
import eng.jAtcSim.lib.exceptions.EBindException;

/**
 *
 * @author Marek
 */
public class PublishedHold{
  private String navaidName;
  @XmlOptional
  private Navaid _navaid;
  private int inboundRadial;
  private String turn;
  @XmlOptional
  private boolean _leftTurn;
  @XmlIgnore
  private Airport _parent;

  public Navaid getNavaid() {
    return _navaid;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public boolean isLeftTurn() {
    return _leftTurn;
  }
  public boolean isRightTurn() {
    return !_leftTurn;
  }
  
  public void bind(){
    Navaid n = getParent().getParent().getNavaids().get(navaidName);
    if (n == null){
      throw new EBindException("Published hold cannot be created. Unknown navaid " + navaidName);
    }
    
    this._navaid = n;
    
    this._leftTurn = this.turn.equals("left");
  }

  void setParent(Airport airport) {
    this._parent = airport;
  }
  
  public Airport getParent(){
    return this._parent;
  }

  @Override
  public String toString() {
    return "Published hold {" + navaidName + "}";
  }
}
