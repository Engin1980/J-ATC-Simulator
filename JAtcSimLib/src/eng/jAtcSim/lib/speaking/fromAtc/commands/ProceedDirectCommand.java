/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.world.Navaid;

/**
 *
 * @author Marek
 */
public class ProceedDirectCommand extends ToNavaidCommand {

  private ProceedDirectCommand() {
    super();
  }

  public ProceedDirectCommand(Navaid navaid) {
    super(navaid);
  }
  
    @Override
  public String toString() {
    return "Direct to "  + navaid.getName() + " {command}";
  }
  
}
