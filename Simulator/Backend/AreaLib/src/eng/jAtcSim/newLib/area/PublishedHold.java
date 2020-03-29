/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

/**
 * @author Marek
 */
public class PublishedHold extends Parentable<Airport> {

  private final Navaid navaid;
  private final int inboundRadial;
  private final boolean leftTurn;

  public PublishedHold(Navaid navaid, int inboundRadial, boolean leftTurn) {
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.leftTurn = leftTurn;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public Navaid getNavaid() {
    return navaid;
  }

  public boolean isLeftTurn() {
    return leftTurn;
  }

  public boolean isRightTurn() {
    return !leftTurn;
  }

  @Override
  public String toString() {
    return "Published hold {" + this.getNavaid().getName() + "}";
  }


}
