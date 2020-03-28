package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.speeches.ICommand;

public class AltitudeRestrictionCommand implements ICommand {
  public static AltitudeRestrictionCommand create(Restriction.eDirection direction, int value) {
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
