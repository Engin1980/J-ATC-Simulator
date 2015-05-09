/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.commands;

import jatcsimlib.world.RunwayThreshold;

/**
 *
 * @author Marek
 */
public class ClearedForTakeoffCommand extends Command {
  private final RunwayThreshold runwayThreshold;

  public ClearedForTakeoffCommand(RunwayThreshold runwayThreshold) {
    this.runwayThreshold = runwayThreshold;
  }

  public RunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }
  
}
