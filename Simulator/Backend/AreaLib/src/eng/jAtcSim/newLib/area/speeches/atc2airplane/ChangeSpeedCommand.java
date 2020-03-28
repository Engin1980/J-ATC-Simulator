package eng.jAtcSim.newLib.area.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.exceptions.ApplicationException;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class ChangeSpeedCommand implements IAtcCommand {

  public enum eRestriction {
    below,
    above,
    exactly
  }

  public static ChangeSpeedCommand create(eRestriction direction, int speedInKts) {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(direction, speedInKts);
    return ret;
  }

  public static ChangeSpeedCommand createResumeOwnSpeed() {
    ChangeSpeedCommand ret = new ChangeSpeedCommand(eRestriction.exactly, null);
    return ret;
  }

  public static IAtcCommand load(XElement element) {
    assert element.getName().equals("speed");
    ChangeSpeedCommand ret;
    XmlLoaderUtils.setContext(element);
    String rs = XmlLoaderUtils.loadString("restriction");
    if (rs.equals("clear")) {
      ret = ChangeSpeedCommand.createResumeOwnSpeed();
    } else {
      eRestriction restriction = Enum.valueOf(eRestriction.class, rs);
      int speed = XmlLoaderUtils.loadInteger("value");
      ret = ChangeSpeedCommand.create(restriction, speed);
    }
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
