package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.HoldCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class HoldCommandXmlLoader extends XmlLoader<HoldCommand> {

  public HoldCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public HoldCommand load(XElement element) {
    assert element.getName().equals("hold");

    HoldCommand ret;

    SmartXmlLoaderUtils.setContext(element);
    String fix = SmartXmlLoaderUtils.loadString("fix");
    Integer inboundRadial = SmartXmlLoaderUtils.loadInteger("inboundRadial", null);
    LeftRight turn = SmartXmlLoaderUtils.loadEnum("turns", LeftRight.class, LeftRight.left);

    if (inboundRadial == null) {
      ret = HoldCommand.createPublished(fix);
    } else {
      ret = HoldCommand.createExplicit(fix, inboundRadial, turn);
    }
    return ret;
  }

  //TODEL
//  public static HoldCommand load(XElement element, Airport parent) {
//    assert element.getName().equals("hold");
//
//    HoldCommand ret;
//
//    XmlLoader.setContext(element);
//    String fix = XmlLoader.loadString("fix");
//    Integer inboundRadial = XmlLoader.loadInteger("inboundRadial", null);
//    String turns = XmlLoader.loadStringRestricted("turns", new String[]{"left", "right"}, null);
//
//    Navaid navaid = parent.getParent().getNavaids().get(fix);
//
//    if (inboundRadial == null && turns == null) {
//      PublishedHold publishedHold = parent.getHolds().getFirst(q -> q.getNavaid().equals(navaid));
//      ret = HoldCommand.create(publishedHold);
//    } else if (inboundRadial == null || turns == null) {
//      throw new ApplicationException("For hold command, both or none of 'inboundRadial' and 'turns' must be set.");
//    } else {
//      ret = HoldCommand.create(navaid, inboundRadial, turns.equals("left"));
//    }
//    return ret;
//  }
}
