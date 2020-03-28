package eng.jAtcSim.newLib.speeches.xml.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.ICommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ThenCommand;

class ThenCommandFactory {

  public static ICommand load(XElement element) {
    assert element.getName().equals("then");
    return ThenCommand.create();
  }
}
