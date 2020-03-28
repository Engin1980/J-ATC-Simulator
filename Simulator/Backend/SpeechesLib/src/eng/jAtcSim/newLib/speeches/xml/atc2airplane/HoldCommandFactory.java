package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.HoldCommand;

public class HoldCommandFactory {
  public static HoldCommand load(XElement element) {
    assert element.getName().equals("hold");

    HoldCommand ret;

    XmlLoaderUtils.setContext(element);
    String fix = XmlLoaderUtils.loadString("fix");
    Integer inboundRadial = XmlLoaderUtils.loadInteger("inboundRadial", null);
    String turns = XmlLoaderUtils.loadStringRestricted("turns", new String[]{"left", "right"}, null);

    if (inboundRadial == null && turns == null) {
      ret = HoldCommand.createPublished(fix);
    } else if (inboundRadial == null || turns == null) {
      throw new ApplicationException("For hold command, both or none of 'inboundRadial' and 'turns' must be set.");
    } else {
      ret = HoldCommand.createExplicit(fix, inboundRadial, turns.equals("left"));
    }
    return ret;
  }

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
