package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;

public class ProceedDirectCommand extends ToNavaidCommand {

  public static ProceedDirectCommand create(String navaidName) {
    ProceedDirectCommand ret = new ProceedDirectCommand(navaidName);
    return ret;
  }

  private ProceedDirectCommand(String navaidName) {
    super(navaidName);
  }

  @Override
  public String toString() {
    return "Direct to " + navaidName + " {command}";
  }

}
