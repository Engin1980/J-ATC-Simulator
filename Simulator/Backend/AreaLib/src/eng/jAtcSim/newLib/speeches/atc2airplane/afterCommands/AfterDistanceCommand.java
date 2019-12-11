package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.sharedLib.xml.XmlLoader;

public class AfterDistanceCommand extends AfterCommandWithNavaid {
  public static AfterDistanceCommand load(XElement element, Airport parent) {
    AfterDistanceCommand ret = new AfterDistanceCommand();
    ret.read(element, parent);
    return ret;
  }

  private double distance;
  private AfterValueExtension extension;

  private AfterDistanceCommand() {
  }

  public double getDistance() {
    return distance;
  }

  public AfterValueExtension getExtension() {
    return extension;
  }

  @Override
  protected void read(XElement element, Airport parent) {
    super.read(element, parent);
    this.distance = XmlLoader.loadInteger(element, "distance");
    this.extension = XmlLoader.loadEnum(element, "extension", AfterValueExtension.class, AfterValueExtension.exactly);
  }
}
