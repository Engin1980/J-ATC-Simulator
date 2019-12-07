package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.area.Navaid;

public class ProceedDirectCommand extends ToNavaidCommand {

  public static ProceedDirectCommand create (Navaid navaid){
    ProceedDirectCommand ret = new ProceedDirectCommand(navaid);
    return ret;
  }

  private ProceedDirectCommand(Navaid navaid) {
    super(navaid);
  }
  
    @Override
  public String toString() {
      return "Direct to "  + navaid.getName() + " {command}";
  }
  
}
