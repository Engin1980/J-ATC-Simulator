package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ChangeHeadingCommand implements ICommand {

  public static ChangeHeadingCommand create(int heading, LeftRightAny direction) {
    ChangeHeadingCommand ret = new ChangeHeadingCommand(heading, direction);
    return ret;
  }

  public static ChangeHeadingCommand createContinueCurrentHeading() {
    ChangeHeadingCommand ret = new ChangeHeadingCommand(null, LeftRightAny.any);
    return ret;
  }

  @XConstructor
  @XmlConstructor
  private ChangeHeadingCommand(){
    this.heading = 0;
    this.direction = LeftRightAny.any;
  }

  private final Integer heading;
  private final LeftRightAny direction;

  private ChangeHeadingCommand(Integer heading, LeftRightAny direction) {
    if (heading != null) {
      EAssert.Argument.isTrue(heading >= 0, sf("Heading cannot be negative (%d).", heading));
      if (heading > 360) {
        heading = heading % 360;
      }
      this.heading = heading;
    } else this.heading = null;
    this.direction = direction;
  }

  public LeftRightAny getDirection() {
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
