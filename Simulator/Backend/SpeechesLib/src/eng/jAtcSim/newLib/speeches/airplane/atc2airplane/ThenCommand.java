/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.speeches.airplane.atc2airplane;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class ThenCommand implements ICommand {

  public static ThenCommand create() {
    ThenCommand ret = new ThenCommand();
    return ret;
  }

  private ThenCommand() {
  }

  @Override
  public String toString() {
    return "then {command}";
  }
}
