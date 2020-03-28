package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.AltitudeRestrictionCommand;

public class AltitudeRestrictionCommandFactory {
  public static ICommand load(XElement element) {
    assert element.getName().equals("AltitudeRestriction") ||
        element.getName().equals("AltitudeRestrictionClear");
    AltitudeRestrictionCommand ret;

    XmlLoader.setContext(element);

    if (element.getName().equals("AltitudeRestrictionClear"))
      ret = AltitudeRestrictionCommand.createClearRestriction();
    else {
      String resString = XmlLoader.loadString("restriction");
      int value = XmlLoader.loadInteger("value");
      AboveBelowExactly restriction;
      switch (resString) {
        case "above":
          restriction = AboveBelowExactly.above;
          break;
        case "below":
          restriction = AboveBelowExactly.below;
          break;
        case "exactly":
          restriction = AboveBelowExactly.exactly;
          break;
        default:
          throw new EEnumValueUnsupportedException(resString);
      }
      ret = AltitudeRestrictionCommand.create(restriction, value);
    }
    return ret;
  }
}
