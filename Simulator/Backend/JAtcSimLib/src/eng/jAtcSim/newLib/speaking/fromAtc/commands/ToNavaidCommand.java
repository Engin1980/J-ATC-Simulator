/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.speaking.fromAtc.commands;

import eng.jAtcSim.newLib.area.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.world.Navaid;

/**
 *
 * @author Marek
 */
public class ToNavaidCommand implements IAtcCommand {
  protected final Navaid navaid;

  protected ToNavaidCommand() {
    this.navaid = null;
  }

  public ToNavaidCommand(Navaid navaid) {
    if (navaid == null) {
      throw new IllegalArgumentException("Argument \"navaid\" cannot be null.");
    }
    
    this.navaid = navaid;
  }

  public Navaid getNavaid() {
    return navaid;
  }
  
}
