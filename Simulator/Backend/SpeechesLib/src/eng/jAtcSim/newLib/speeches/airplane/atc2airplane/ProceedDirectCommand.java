package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.newXmlUtils.annotations.XmlConstructor;

public class ProceedDirectCommand extends ToNavaidCommand {

  @XmlConstructor
  private ProceedDirectCommand(){
    super("?");
  }

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
