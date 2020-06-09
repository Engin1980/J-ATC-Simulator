package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.exceptions.EApplicationException;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class ChangeSpeedCommand implements ICommand {

  public static ChangeSpeedCommand create(AboveBelowExactly direction, int speedInKts) {
    EAssert.Argument.isTrue(speedInKts > 0);
    ChangeSpeedCommand ret = new ChangeSpeedCommand(direction, speedInKts);
    return ret;
  }

  public static ChangeSpeedCommand createResumeOwnSpeed() {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(AboveBelowExactly.exactly, null);
    return ret;
  }

  private final Restriction restriction;

  private ChangeSpeedCommand(AboveBelowExactly restriction, Integer value) {
    if (value == null)
      this.restriction = null;
    else
      this.restriction = new Restriction(restriction, value);
  }

  public Restriction getRestriction() {
    if (isResumeOwnSpeed())
      throw new EApplicationException("Unable to call this on 'ResumeOwnSpeed' command.");
    return this.restriction;
  }

  public boolean isResumeOwnSpeed() {
    return this.restriction == null;
  }

  @Override
  public String toString() {
    if (this.restriction == null) {
      return "Resume own speed {command}";
    } else {
      switch (this.restriction.direction) {
        case above:
          return "Speed at least " + this.restriction.value + "kts {command}";
        case below:
          return "Speed at most " + this.restriction.value + "kts {command}";
        case exactly:
          return "Speed exactly " + this.restriction.value + "kts {command}";
        default:
          throw new EEnumValueUnsupportedException(this.restriction);
      }
    }
  }
}
