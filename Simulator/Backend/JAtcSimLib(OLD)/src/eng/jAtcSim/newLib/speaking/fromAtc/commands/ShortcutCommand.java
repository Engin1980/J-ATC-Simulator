/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.area.speaking.fromAtc.commands;

import eng.jAtcSim.newLib.world.Navaid;

/**
 *
 * @author Marek
 */
public class ShortcutCommand extends ToNavaidCommand {

  private ShortcutCommand() {
    super();
  }

  public ShortcutCommand(Navaid navaid) {
    super(navaid);
  }

  @Override
  public String toString(){
    String ret = "Shortcut to " + navaid.getName() + " {command}";

    return ret;
  }

}
