package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.sharedLib.exceptions.ApplicationException;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

import javax.xml.bind.annotation.XmlElement;

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

  public static ChangeHeadingCommand load(XElement source){
    assert source.getName().equals("heading");

    XmlLoader.setContext(source);
    String dirS = XmlLoader.loadString("direction", "nearest");
    eDirection dir;
    if (dirS.equals("nearest"))
      dir = eDirection.any;
    else
      dir = Enum.valueOf(eDirection.class, dirS);
    int hdg = XmlLoader.loadInteger("value");
    ChangeHeadingCommand ret  = new ChangeHeadingCommand(hdg,dir);
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
