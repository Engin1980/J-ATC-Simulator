package eng.jAtcSim.newLib.speeches;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.SwitchCaseNotFoundException;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands.*;

public class XmlAfterCommandFactory {

  public static ICommand load(XElement element) {
    throw new UnsupportedOperationException();
  }

  private static ICommand loadAfterCommand(XElement element) {
    assert element.getName().equals("after");
    XmlLoaderUtils.setContext(element);
    String property = XmlLoaderUtils.loadStringRestricted("property",
        new String[]{"speed", "altitude", "heading", "distance", "radial", "navaid"});
    switch (property) {
      case "speed":
        return AfterSpeedCommandXmlLoader.load(element);
      case "altitude":
        return AfterAltitudeCommandXmlLoader.load(element);
      case "heading":
        return AfterHeadingCommandXmlLoader.load(element);
      case "distance":
        return AfterDistanceCommandXmlLoader.load(element);
      case "radial":
        return AfterRadialCommandXmlLoader.load(element);
      case "navaid":
        return AfterNavaidCommandXmlLoader.load(element);
      default:
        throw new SwitchCaseNotFoundException(property);
    }
  }

  private XmlAfterCommandFactory() {
  }
}
