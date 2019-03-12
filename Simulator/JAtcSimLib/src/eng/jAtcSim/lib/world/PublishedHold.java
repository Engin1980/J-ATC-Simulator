/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

/**
 *
 * @author Marek
 */
public class PublishedHold{
  private Navaid navaid;
  private int inboundRadial;
  private boolean _leftTurn;
  private Airport _parent;

  public Navaid getNavaid() {
    return navaid;
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

  public PublishedHold(Navaid navaid, int inboundRadial, boolean _leftTurn, Airport _parent) {
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this._leftTurn = _leftTurn;
    this._parent = _parent;
  }

  void setParent(Airport airport) {
    this._parent = airport;
  }
  
  public Airport getParent(){
    return this._parent;
  }

  @Override
  public String toString() {
    return "Published hold {" + this.getNavaid().getName() + "}";
  }
}
