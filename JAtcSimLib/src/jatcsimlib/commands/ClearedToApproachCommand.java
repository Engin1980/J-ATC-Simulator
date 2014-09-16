/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.commands;

import jatcsimlib.world.Approach;

/**
 *
 * @author Marek
 */
public class ClearedToApproachCommand extends Command {
  private final Approach approach;

  public ClearedToApproachCommand(Approach approach) {
    this.approach = approach;
  }

  public Approach getApproach() {
    return approach;
  }

  @Override
  public String toString() {
    return "Cleared-to-app{" + approach.getType() + " " + approach.getParent().getName() + '}';
  }
}
