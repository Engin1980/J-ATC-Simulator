package eng.jAtcSim.newLib.area.speeches.atc2airplane.afterCommands;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.shared.xml.XmlLoaderUtils;

public abstract class AfterCommandWithNavaid extends AfterCommand {
  private Navaid navaid;

  protected void read(XElement element, Airport parent) {
    String fix = XmlLoaderUtils.loadString(element, "fix");
    this.navaid = parent.getParent().getNavaids().getOrGenerate(fix, parent);
  }

  protected AfterCommandWithNavaid() {
  }

  public Navaid getNavaid() {
    return navaid;
  }
}
