package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public class SetAltitudeRestriction implements IAtcCommand {

  private final Restriction restriction;

  public SetAltitudeRestriction() {
    this.restriction = null;
  }

  public SetAltitudeRestriction(Restriction restriction) {
    this.restriction = restriction;
  }

  public Restriction getRestriction() {
    return restriction;
  }

  @Override
  public String toString() {
    if (restriction != null)
      return "Altitude restriction " + restriction.toString() + " {command}";
    else
      return "Altitude restriction remove {command}";
  }

}
