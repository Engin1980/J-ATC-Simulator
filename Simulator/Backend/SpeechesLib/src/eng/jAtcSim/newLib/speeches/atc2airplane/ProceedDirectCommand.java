package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.Airport;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;

public class ProceedDirectCommand extends ToNavaidCommand {

  public static ProceedDirectCommand create(Navaid navaid) {
    ProceedDirectCommand ret = new ProceedDirectCommand(navaid);
    return ret;
  }

  public static IAtcCommand load(XElement element, Airport airport) {
    assert element.getName().equals("proceedDirect");
    String navaidName = element.getAttribute("fix");
    Navaid navaid = airport.getParent().getNavaids().getOrGenerate(navaidName, airport);
    ProceedDirectCommand ret = new ProceedDirectCommand(navaid);
    return ret;
  }

  private ProceedDirectCommand(Navaid navaid) {
    super(navaid);
  }

  @Override
  public String toString() {
    return "Direct to " + navaid.getName() + " {command}";
  }

}
