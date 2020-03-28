package eng.jAtcSim.newLib.area.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class AltitudeRestrictionCommand implements IAtcCommand {
  public static AltitudeRestrictionCommand create(AboveBelowExactly direction, int value) {
    Restriction res = new Restriction(direction, value);
    AltitudeRestrictionCommand ret = new AltitudeRestrictionCommand(res);
    return ret;
  }

  public static AltitudeRestrictionCommand createClearRestriction() {
    AltitudeRestrictionCommand ret = new AltitudeRestrictionCommand(null);
    return ret;
  }

  public static IAtcCommand load(XElement element) {
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
  private final Restriction restriction;

  private AltitudeRestrictionCommand(Restriction restriction) {
    this.restriction = restriction;
  }

  public Restriction getRestriction() {
    if (isClearRestriction())
      throw new ApplicationException("This method should not be called when 'isClearRestriction()' is true.");
    return restriction;
  }

  public boolean isClearRestriction() {
    return this.restriction == null;
  }

  @Override
  public String toString() {
    if (restriction != null)
      return "Altitude restriction " + restriction.toString() + " {command}";
    else
      return "Altitude restriction remove {command}";
  }
}
