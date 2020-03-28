package eng.jAtcSim.newLib.area.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AfterSpeedCommand extends AfterCommand {
  public static AfterSpeedCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterSpeedCommand ret = new AfterSpeedCommand();
    ret.speed = XmlLoaderUtils.loadInteger("value");
    ret.extension = XmlLoaderUtils.loadEnum("extension", AfterValueExtension.class, AfterValueExtension.exactly);
    return ret;
  }

  private int speed;
  private AfterValueExtension extension;

  private AfterSpeedCommand() {
  }

  public int getSpeed() {
    return speed;
  }

  public AfterValueExtension getExtension() {
    return extension;
  }
}
