package eng.jAtcSim.newLib.routes;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Area;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.*;

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
        return ChangeAltitudeCommand.load(element, parent);
      case "altitudeRouteRestriction":
      case "altitudeRouteRestrictionClear":
        return AltitudeRestrictionCommand.load(element, parent);
      case "heading":
        return ChangeHeadingCommand.load(element, parent);
      case "hold":
        return HoldCommand.load(element, parent);
      case "after":
        return AfterCommand.load(element, parent);
      default:
        throw new EEnumValueUnsupportedException(element.getName());
    }
  }
}
