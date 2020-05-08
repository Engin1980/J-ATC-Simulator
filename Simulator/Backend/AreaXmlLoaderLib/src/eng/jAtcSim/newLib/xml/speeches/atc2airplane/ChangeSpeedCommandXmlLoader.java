package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeSpeedCommand;

public class ChangeSpeedCommandXmlLoader implements IXmlLoader<ChangeSpeedCommand> {
  @Override
  public ChangeSpeedCommand load(XElement element) {
    assert element.getName().equals("speed");
    ChangeSpeedCommand ret;
    XmlLoaderUtils.setContext(element);
    String rs = XmlLoaderUtils.loadString("restriction");
    if (rs.equals("clear")) {
      ret = ChangeSpeedCommand.createResumeOwnSpeed();
    } else {
      AboveBelowExactly restriction = Enum.valueOf(AboveBelowExactly.class, rs);
      int speed = XmlLoaderUtils.loadInteger("value");
      ret = ChangeSpeedCommand.create(restriction, speed);
    }
    return ret;
  }
}
