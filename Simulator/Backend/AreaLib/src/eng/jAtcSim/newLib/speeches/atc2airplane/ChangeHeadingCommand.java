package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.sharedLib.exceptions.ApplicationException;

public class ChangeHeadingCommand implements IAtcCommand {

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
      if (heading != 360) {
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
    if (this.heading == null)
      throw new ApplicationException("Unable to return heading, when 'ChangeHeadingCommant' represents 'Fly current heading'");
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
