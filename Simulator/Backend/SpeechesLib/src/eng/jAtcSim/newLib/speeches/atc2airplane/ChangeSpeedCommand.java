package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.speeches.ICommand;

public class ChangeSpeedCommand implements  ICommand {

  public enum eRestriction {
    below,
    above,
    exactly
  }

  public static ChangeSpeedCommand create(eRestriction direction, int speedInKts) {
    EAssert.Argument.isTrue(speedInKts > 0);
    ChangeSpeedCommand ret = new ChangeSpeedCommand(direction, speedInKts);
    return ret;
  }

  public static ChangeSpeedCommand createResumeOwnSpeed() {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(eRestriction.exactly, null);
    return ret;
  }

  private final eRestriction restriction;
  private final Integer value;

  private ChangeSpeedCommand(eRestriction restriction, Integer value) {
    this.restriction = restriction;
    this.value = value;
  }

  public eRestriction getDirection() {
    if (isResumeOwnSpeed())
      throw new ApplicationException("Unable to call this on 'ResumeOwnSpeed' command.");
    return this.restriction;
  }

  public int getSpeedInKts() {
    if (isResumeOwnSpeed())
      throw new ApplicationException("Unable to call this on 'ResumeOwnSpeed' command.");
    return this.value;
  }

  public boolean isResumeOwnSpeed() {
    return this.value == null;
  }

  @Override
  public String toString() {
    if (isResumeOwnSpeed()) {
      return "Resume own speed {command}";
    } else {
      switch (this.restriction) {
        case above:
          return "Speed at least " + this.value + "kts {command}";
        case below:
          return "Speed at most " + this.value + "kts {command}";
        case exactly:
          return "Speed exactly " + this.value + "kts {command}";
        default:
          throw new EEnumValueUnsupportedException(this.restriction);
      }
    }
  }
}