package eng.jAtcSim.newLib.area.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AfterHeadingCommand extends AfterCommand {
  public static AfterHeadingCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterHeadingCommand ret = new AfterHeadingCommand();
    ret.heading = XmlLoaderUtils.loadInteger("value");
    ret.extension = XmlLoaderUtils.loadEnum("extension", AfterValueExtension.class, AfterValueExtension.exactly);
    return ret;
  }

  private int heading;
  private AfterValueExtension extension;

  private AfterHeadingCommand() {
  }

  public int getHeading() {
    return heading;
  }

  public AfterValueExtension getExtension() {
    return extension;
  }
}
