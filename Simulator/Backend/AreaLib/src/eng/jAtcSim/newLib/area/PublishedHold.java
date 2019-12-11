/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

/**
 * @author Marek
 */
public class PublishedHold extends Parentable<Airport> {

  public static PublishedHold load(XElement source, Airport airport) {
    PublishedHold ret = new PublishedHold();
    ret.setParent(airport);
    ret.read(source);
    return ret;
  }

  private Navaid navaid;
  private int inboundRadial;
  private boolean leftTurn;

  private PublishedHold() {
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

  private void read(XElement source) {
    XmlLoader.setContext(source);
    String navaidName = XmlLoader.loadString("name");
    this.navaid = this.getParent().getParent().getNavaids().get(navaidName);

    this.inboundRadial = XmlLoader.loadInteger("inboundRadial");
    this.leftTurn = XmlLoader.loadStringRestricted("turn", new String[]{"left", "right"}).equals("left");
  }
}
