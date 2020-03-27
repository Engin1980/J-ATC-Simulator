package eng.jAtcSim.newLib.speeches.xml;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeSpeedCommand;

public class ChangeSpeedCommandFactory {
  public static ChangeSpeedCommand load(XElement element) {
    assert element.getName().equals("speed");
    ChangeSpeedCommand ret;
    XmlLoader.setContext(element);
    String rs = XmlLoader.loadString("restriction");
    if (rs.equals("clear")) {
      ret = ChangeSpeedCommand.createResumeOwnSpeed();
    } else {
      ChangeSpeedCommand.eRestriction restriction = Enum.valueOf(ChangeSpeedCommand.eRestriction.class, rs);
      int speed = XmlLoader.loadInteger("value");
      ret = ChangeSpeedCommand.create(restriction, speed);
    }
    return ret;
  }
}
