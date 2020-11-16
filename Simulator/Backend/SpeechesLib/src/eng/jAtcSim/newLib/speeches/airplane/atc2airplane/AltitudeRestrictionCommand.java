package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.newXmlUtils.annotations.XmlConstructor;

public class AltitudeRestrictionCommand implements ICommand {
  public static AltitudeRestrictionCommand create(AboveBelowExactly direction, int value) {
    EAssert.Argument.isTrue(value >= 0);
    Restriction res = new Restriction(direction, value);
    AltitudeRestrictionCommand ret = new AltitudeRestrictionCommand(res);
    return ret;
  }

  public static AltitudeRestrictionCommand createClearRestriction() {
    AltitudeRestrictionCommand ret = new AltitudeRestrictionCommand(null);
    return ret;
  }

  private final Restriction restriction;

  @XmlConstructor
  private AltitudeRestrictionCommand() {
    this.restriction = null;
  }

  private AltitudeRestrictionCommand(Restriction restriction) {
    this.restriction = restriction;
  }

  public Restriction getRestriction() {
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
