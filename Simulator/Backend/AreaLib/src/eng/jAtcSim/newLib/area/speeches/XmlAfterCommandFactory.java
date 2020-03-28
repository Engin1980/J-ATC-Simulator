package eng.jAtcSim.newLib.area.speeches;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.SwitchCaseNotFoundException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.afterCommands.*;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class XmlAfterCommandFactory {


  public static IAtcCommand load(XElement element, Airport parent) {
    assert element.getName().equals("after");
    XmlLoaderUtils.setContext(element);
    String property = XmlLoaderUtils.loadStringRestricted("property",
        new String[]{"speed", "altitude", "heading","distance","radial","navaid"});
    switch (property){
      case "speed":
        return AfterSpeedCommand.load(element);
      case "altitude":
        return AfterAltitudeCommand.load(element);
      case "heading":
        return AfterHeadingCommand.load(element);
      case "distance":
            return AfterDistanceCommand.load(element,parent);
      case "radial":
        return AfterRadialCommand.load(element,parent);
      case "navaid":
        return AfterNavaidCommand.load(element,parent);
      default:
        throw new SwitchCaseNotFoundException(property);
    }
  }

  private XmlAfterCommandFactory() {
  }
}
