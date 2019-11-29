/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.area;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.area.xml.XmlLoader;

/**
 * @author Marek
 */
public class PublishedHold extends Parentable<Airport> {

  public static IList<PublishedHold> loadList(IReadOnlyList<XElement> sources, NavaidList navaids) {
    IList<PublishedHold> ret = new EList<>();
    for (XElement source : sources) {
      PublishedHold hold = PublishedHold.load(source, navaids);
      ret.add(hold);
    }
    return ret;
  }

  private static PublishedHold load(XElement source, NavaidList navaids) {
    XmlLoader.setContext(source);
    String navaidName = XmlLoader.loadString("name");
    Navaid navaid = navaids.get(navaidName);

    int inboundRadial = XmlLoader.loadInteger("inboundRadial");
    boolean leftTurn = XmlLoader.loadStringRestricted("turn", new String[]{"left", "right"}).equals("left");

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, leftTurn);
    return ret;
  }

  private Navaid navaid;
  private int inboundRadial;
  private boolean leftTurn;

  private PublishedHold(Navaid navaid, int inboundRadial, boolean leftTurn) {
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
