/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands.afters;

import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.world.Navaid;

/**
 *
 * @author Marek
 */
public class AfterNavaidCommand extends AfterCommand {
  private final Navaid navaid;

  @XmlConstructor
  private AfterNavaidCommand() {
    navaid = null;
  }

  public AfterNavaidCommand(Navaid navaid) {
    if (navaid == null) {
      throw new IllegalArgumentException("Argument \"navaid\" cannot be null.");
    }

    this.navaid = navaid;
  }

  public Navaid getNavaid() {
    return navaid;
  }
  
    @Override
  public String toString() {
    return "AN{"+ navaid.getName() + '}';
  }
  
}
