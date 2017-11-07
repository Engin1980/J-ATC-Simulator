/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.speaking.fromAtc.commands;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;
import jatcsimlib.world.Approach;

/**
 *
 * @author Marek
 */
public class ClearedToApproachCommand implements IAtcCommand {
  private final Approach approach;

  public ClearedToApproachCommand(Approach approach) {
    this.approach = approach;
  }

  public Approach getApproach() {
    return approach;
  }

  @Override
  public String toString() {
    return "Cleared for approach " + approach.getType() + " at " + approach.getParent().getName() + " {command}";
  }
}
