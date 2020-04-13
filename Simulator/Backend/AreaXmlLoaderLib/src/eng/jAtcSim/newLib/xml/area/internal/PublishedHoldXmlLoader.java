package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
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
    LeftRight turn = XmlLoaderUtils.loadEnum("turn", LeftRight.class);

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, turn);
    return ret;
  }
}
