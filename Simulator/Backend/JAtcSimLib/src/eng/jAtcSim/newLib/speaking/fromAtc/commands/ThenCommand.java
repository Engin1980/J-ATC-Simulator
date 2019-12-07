/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.speaking.fromAtc.commands;

import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ThenCommand implements IAtcCommand {
 
    @Override
  public String toString() {
    return "then {command}";
  }
}
