package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.shared.xml.XmlLoader;

public class AfterRadialCommand extends AfterCommandWithNavaid {
  public static AfterRadialCommand load(XElement element, Airport parent) {
    AfterRadialCommand ret = new AfterRadialCommand();
    ret.read(element, parent);
    return ret;
  }

  private int radial;
  private AfterValueExtension extension;

  private AfterRadialCommand() {
  }

  public int getRadial() {
    return radial;
  }

  public AfterValueExtension getExtension() {
    return extension;
  }

  @Override
  protected void read(XElement element, Airport parent) {
    super.read(element, parent);
    this.radial = XmlLoader.loadInteger(element, "radial");
    this.extension = XmlLoader.loadEnum(element, "extension", AfterValueExtension.class, AfterValueExtension.exactly);
  }
}
