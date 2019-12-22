/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.speeches.atc2airplane;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.area.speeches.IAtcCommand;

public class ThenCommand implements IAtcCommand {

  public static ThenCommand create(){
    ThenCommand ret = new ThenCommand();
    return ret;
  }

  public static IAtcCommand load(XElement element) {
    assert element.getName().equals("then");
    return new ThenCommand();
  }

  private ThenCommand() {
  }

  @Override
  public String toString() {
    return "then {command}";
  }
}
