package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class AfterAltitudeCommand extends AfterCommand {
  public static AfterAltitudeCommand load(XElement element) {
    XmlLoader.setContext(element);

    AfterAltitudeCommand ret = new AfterAltitudeCommand();
    ret.altitude = XmlLoader.loadInteger("value");
    ret.extension = XmlLoader.loadEnum("extension", AfterValueExtension.class, AfterValueExtension.exactly);
    return ret;
  }

  private int altitude;
  private AfterValueExtension extension;

  private AfterAltitudeCommand() {
  }

  public int getAltitude() {
    return altitude;
  }

  public AfterValueExtension getExtension() {
    return extension;
  }
}
