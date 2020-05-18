package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.SmartXmlLoaderUtils;

class Shared {
  public static AboveBelowExactly loadAfterValuePosition(XElement element) {
    return SmartXmlLoaderUtils.loadEnum(element, "extension", AboveBelowExactly.class, AboveBelowExactly.exactly);
  }

  public static String loadNavaidName(XElement element) {
    return SmartXmlLoaderUtils.loadString("navaidName");
  }
}
