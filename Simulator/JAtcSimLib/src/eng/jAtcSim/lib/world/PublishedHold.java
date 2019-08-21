/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.lib.world.xml.XmlLoader;

/**
 *
 * @author Marek
 */
public class PublishedHold{

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
    String navaidName = XmlLoader.loadString("name",true);
    Navaid navaid = navaids.get(navaidName);

    int inboundRadial = XmlLoader.loadInteger("inboundRadial", true);
    boolean leftTurn = XmlLoader.loadString("turn",true).equals("left");

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, leftTurn);
    return ret;
  }

  private Navaid navaid;
  private int inboundRadial;
  private boolean leftTurn;
  private Airport _parent;

  public Navaid getNavaid() {
    return navaid;
  }

  public int getInboundRadial() {
    return inboundRadial;
  }

  public boolean isLeftTurn() {
    return leftTurn;
  }
  public boolean isRightTurn() {
    return !leftTurn;
  }

  private PublishedHold(Navaid navaid, int inboundRadial, boolean leftTurn, Airport _parent) {
    this.navaid = navaid;
    this.inboundRadial = inboundRadial;
    this.leftTurn = leftTurn;
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
