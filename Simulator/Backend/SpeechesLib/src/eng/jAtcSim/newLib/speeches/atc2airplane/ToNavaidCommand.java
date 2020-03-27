package eng.jAtcSim.newLib.speeches.atc2airplane;


import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.IAtcCommand;

public abstract class ToNavaidCommand implements IAtcCommand {
  protected final String navaidName;

  protected ToNavaidCommand(String navaidName) {
    EAssert.Argument.isNonEmptyString(navaidName, "navaidName");
    this.navaidName = navaidName;
  }

  public String getNavaidName() {
    return navaidName;
  }
}
