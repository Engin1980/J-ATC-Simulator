package eng.jAtcSim.newLib.speeches.xml;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.speeches.IAtcCommand;
import eng.jAtcSim.newLib.speeches.atc2airplane.ThenCommand;

class ThenCommandFactory {

  public static IAtcCommand load(XElement element) {
    assert element.getName().equals("then");
    return ThenCommand.create();
  }
}
