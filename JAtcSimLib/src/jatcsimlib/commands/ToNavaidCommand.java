/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.commands;

import jatcsimlib.world.Navaid;

/**
 *
 * @author Marek
 */
public class ToNavaidCommand  extends Command{
  protected final Navaid navaid;

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
