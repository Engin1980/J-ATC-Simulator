package eng.jAtcSim.newLib.area.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.NavaidList;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class PublishedHoldXmlLoader extends XmlLoaderWithNavaids<PublishedHold> {
  PublishedHoldXmlLoader(NavaidList navaids) {
    super(navaids);
  }

  @Override
  public PublishedHold load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String navaidName = XmlLoaderUtils.loadString("name");
    Navaid navaid = navaids.get(navaidName);

    int inboundRadial = XmlLoaderUtils.loadInteger("inboundRadial");
    boolean leftTurn = XmlLoaderUtils.loadStringRestricted("turn", new String[]{"left", "right"}).equals("left");

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, leftTurn);
    return ret;
  }
}
