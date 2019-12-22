package eng.jAtcSim.newLib.area.routes;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.SwitchCaseNotFoundException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.area.speeches.XmlAfterCommandFactory;
import eng.jAtcSim.newLib.area.speeches.atc2airplane.*;

public class XmlRouteFactory {
  public static IAtcCommand load(XElement element, Airport parent) {
    switch (element.getName()) {
      case "then":
        return ThenCommand.load(element);
      case "proceedDirect":
        return ProceedDirectCommand.load(element, parent);
      case "speed":
        return ChangeSpeedCommand.load(element);
      case "altitude":
        return ChangeAltitudeCommand.load(element);
      case "altitudeRouteRestriction":
      case "altitudeRouteRestrictionClear":
        return AltitudeRestrictionCommand.load(element);
      case "heading":
        return ChangeHeadingCommand.load(element);
      case "hold":
        return HoldCommand.load(element, parent);
      case "after":
        return XmlAfterCommandFactory.load(element, parent);
      default:
        throw new SwitchCaseNotFoundException(element.getName());
    }
  }

  private XmlRouteFactory() {
  }
}
