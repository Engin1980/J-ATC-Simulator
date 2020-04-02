package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class PublishedHoldXmlLoader extends XmlLoader<PublishedHold> {

  PublishedHoldXmlLoader(Context context) {
    super(context);
  }

  @Override
  public PublishedHold load(XElement source) {
    XmlLoaderUtils.setContext(source);
    String navaidName = XmlLoaderUtils.loadString("name");
    Navaid navaid = context.area.navaids.get(navaidName);

    int inboundRadial = XmlLoaderUtils.loadInteger("inboundRadial");
    boolean leftTurn = XmlLoaderUtils.loadStringRestricted("turn", new String[]{"left", "right"}).equals("left");

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, leftTurn);
    return ret;
  }
}
