package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.xml.IXmlLoader;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ThenCommand;

public class ThenCommandXmlLoader implements IXmlLoader<ThenCommand> {

  @Override
  public ThenCommand load(XElement element) {
    assert element.getName().equals("then");
    return ThenCommand.create();
  }
}
