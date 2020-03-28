package eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands.AfterValuePosition;

class Shared {
  public static AfterValuePosition loadAfterValuePosition(XElement element) {
    return XmlLoaderUtils.loadEnum(element, "extension", AfterValuePosition.class, AfterValuePosition.exactly);
  }

  public static String loadNavaidName(XElement element) {
    return XmlLoaderUtils.loadString("navaidName");
  }
}
