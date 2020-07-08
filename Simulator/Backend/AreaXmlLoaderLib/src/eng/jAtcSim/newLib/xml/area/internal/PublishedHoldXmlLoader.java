package eng.jAtcSim.newLib.xml.area.internal;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.xml.area.internal.context.Context;

public class PublishedHoldXmlLoader extends XmlLoader<PublishedHold> {

  PublishedHoldXmlLoader(Context context) {
    super(context);
  }

  @Override
  public PublishedHold load(XElement source) {
    SmartXmlLoaderUtils.setContext(source);
    String navaidName = SmartXmlLoaderUtils.loadString("navaidName");
    Navaid navaid = context.area.navaids.get(navaidName);

    int inboundRadial = SmartXmlLoaderUtils.loadInteger("inboundRadial");
    LeftRight turn = SmartXmlLoaderUtils.loadEnum("turn", LeftRight.class);

    PublishedHold ret = new PublishedHold(navaid, inboundRadial, turn);
    return ret;
  }
}
