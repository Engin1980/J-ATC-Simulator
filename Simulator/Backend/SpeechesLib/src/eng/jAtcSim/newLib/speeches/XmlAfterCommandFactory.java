package eng.jAtcSim.newLib.speeches;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.SwitchCaseNotFoundException;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.xml.atc2airplane.afterCommands.*;

public class XmlAfterCommandFactory {

  public static IAtcCommand load(XElement element) {
    throw new UnsupportedOperationException();
  }

  private static IAtcCommand loadAfterCommand(XElement element) {
    assert element.getName().equals("after");
    XmlLoader.setContext(element);
    String property = XmlLoader.loadStringRestricted("property",
        new String[]{"speed", "altitude", "heading", "distance", "radial", "navaid"});
    switch (property) {
      case "speed":
        return AfterSpeedCommandFactory.load(element);
      case "altitude":
        return AfterAltitudeCommandFactory.load(element);
      case "heading":
        return AfterHeadingCommandFactory.load(element);
      case "distance":
        return AfterDistanceCommandFactory.load(element);
      case "radial":
        return AfterRadialCommandFactory.load(element);
      case "navaid":
        return AfterNavaidCommandFactory.load(element);
      default:
        throw new SwitchCaseNotFoundException(property);
    }
  }

  private XmlAfterCommandFactory() {
  }
}
