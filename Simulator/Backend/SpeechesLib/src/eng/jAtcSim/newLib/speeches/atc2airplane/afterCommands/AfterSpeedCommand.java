package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class AfterSpeedCommand extends AfterCommand {
  public static AfterSpeedCommand load(XElement element) {
    XmlLoader.setContext(element);

    AfterSpeedCommand ret = new AfterSpeedCommand();
    ret.speed = XmlLoader.loadInteger("value");
    ret.extension = XmlLoader.loadEnum("extension", AfterValueExtension.class, AfterValueExtension.exactly);
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
