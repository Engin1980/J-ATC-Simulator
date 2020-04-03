package eng.jAtcSim.newLib.speeches.atc2airplane.afterCommands;

import eng.eSystem.validation.EAssert;

public abstract class AfterCommandWithNavaid extends AfterCommand {
  private final String navaidName;

//  protected void read(XElement element, Airport parent) {
//    String fix = XmlLoader.loadString(element, "fix");
//    this.navaid = parent.getParent().getNavaids().getOrGenerate(fix, parent);
//  }

  protected AfterCommandWithNavaid(String navaidName, AfterValuePosition position) {
    super(position);
    EAssert.Argument.isNonemptyString(navaidName);
    this.navaidName = navaidName;
  }

  public String getNavaidName() {
    return navaidName;
  }
}
