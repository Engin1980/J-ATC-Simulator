package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.atc2airplane.ProceedDirectCommand;

public class ProceedDirectCommandFactory {
  public static ProceedDirectCommand load(XElement element) {
    assert element.getName().equals("proceedDirect");
    String fix = element.getAttribute("fix");
    ProceedDirectCommand ret = ProceedDirectCommand.create(fix);
    return ret;
  }

//  public static ProceedDirectCommand load(XElement element, Airport airport) {
//    assert element.getName().equals("proceedDirect");
//    String navaidName = element.getAttribute("fix");
//    Navaid navaid = airport.getParent().getNavaids().getOrGenerate(navaidName, airport);
//    ProceedDirectCommand ret = new ProceedDirectCommand(navaid);
//    return ret;
//  }
}
