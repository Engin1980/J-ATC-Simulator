package eng.jAtcSim.newLib.xml.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;
import eng.jAtcSim.newLib.xml.area.internal.XmlLoader;
import eng.jAtcSim.newLib.xml.area.internal.context.LoadingContext;

public class ProceedDirectCommandXmlLoader extends XmlLoader<ProceedDirectCommand> {

  public ProceedDirectCommandXmlLoader(LoadingContext data) {
    super(data);
  }

  @Override
  public ProceedDirectCommand load(XElement element) {
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
