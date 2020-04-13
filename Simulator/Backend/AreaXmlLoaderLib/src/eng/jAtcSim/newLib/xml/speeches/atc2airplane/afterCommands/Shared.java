package eng.jAtcSim.newLib.xml.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

class Shared {
  public static AboveBelowExactly loadAfterValuePosition(XElement element) {
    return XmlLoaderUtils.loadEnum(element, "extension", AboveBelowExactly.class, AboveBelowExactly.exactly);
  }

  public static String loadNavaidName(XElement element) {
    return XmlLoaderUtils.loadString("navaidName");
  }
}
