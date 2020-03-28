package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.ICommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ChangeHeadingCommand implements  ICommand {

  public enum eDirection {
    any,
    left,
    right
  }

  public static ChangeHeadingCommand create(int heading, eDirection direction) {
    ChangeHeadingCommand ret = new ChangeHeadingCommand(heading, direction);
    return ret;
  }

  public static ChangeHeadingCommand createContinueCurrentHeading() {
    ChangeHeadingCommand ret = new ChangeHeadingCommand(null, eDirection.any);
    return ret;
  }

  private final Integer heading;
  private final eDirection direction;

  private ChangeHeadingCommand(Integer heading, eDirection direction) {
    if (heading != null) {
      EAssert.Argument.isTrue(heading >= 0, sf("Heading cannot be negative (%d).", heading));
      if (heading > 360) {
        heading = heading % 360;
      }
      this.heading = heading;
    } else this.heading = null;
    this.direction = direction;
  }

  public eDirection getDirection() {
    return direction;
  }

  public int getHeading() {
    EAssert.isNotNull(this.heading,
        "Unable to return heading, when 'ChangeHeadingCommand' represents 'Fly current heading'");
    return heading;
  }

  public boolean isCurrentHeading() {
    return heading == null;
  }

  @Override
  public String toString() {
    if (isCurrentHeading()) {
      return "Fly current heading {command}";
    } else {
      return "Fly heading " + heading + " {command}";
    }
  }

}
