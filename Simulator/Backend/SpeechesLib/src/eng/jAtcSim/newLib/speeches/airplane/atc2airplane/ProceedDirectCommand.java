package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;


import exml.annotations.XConstructor;

public class ProceedDirectCommand extends ToNavaidCommand {

  @XConstructor

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
