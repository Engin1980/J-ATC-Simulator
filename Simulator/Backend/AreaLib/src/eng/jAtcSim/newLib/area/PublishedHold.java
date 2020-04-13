/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.jAtcSim.newLib.shared.enums.LeftRight;

/**
 * @author Marek
 */
public class PublishedHold extends Parentable<Airport> {

  private final Navaid navaid;
  private final int inboundRadial;
  private final LeftRight turn;

  public PublishedHold(Navaid navaid, int inboundRadial, LeftRight turn) {
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.turn = turn;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public LeftRight getTurn() {
    return turn;
  }

  @Override
  public String toString() {
    return "Published hold {" + this.getNavaid().getName() + "}";
  }


}
