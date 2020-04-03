package eng.jAtcSim.newLib.speeches.atc2airplane;


import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.ICommand;

public abstract class ToNavaidCommand implements ICommand {
  protected final String navaidName;

  protected ToNavaidCommand(String navaidName) {
    EAssert.Argument.isNonemptyString(navaidName, "navaidName");
    this.navaidName = navaidName;
  }

  public String getNavaidName() {
    return navaidName;
  }
}
