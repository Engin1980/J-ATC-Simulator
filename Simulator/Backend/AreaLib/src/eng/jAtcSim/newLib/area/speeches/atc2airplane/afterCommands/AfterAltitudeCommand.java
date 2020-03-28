package eng.jAtcSim.newLib.area.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public class AfterAltitudeCommand extends AfterCommand {
  public static AfterAltitudeCommand load(XElement element) {
    XmlLoaderUtils.setContext(element);

    AfterAltitudeCommand ret = new AfterAltitudeCommand();
    ret.altitude = XmlLoaderUtils.loadInteger("value");
    ret.extension = XmlLoaderUtils.loadEnum("extension", AfterValueExtension.class, AfterValueExtension.exactly);
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
