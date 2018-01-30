/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;
import jatcsimlib.world.RunwayThreshold;

/**
 *
 * @author Marek
 */
public class ClearedForTakeoffCommand implements IAtcCommand {
  private final RunwayThreshold runwayThreshold;

  public ClearedForTakeoffCommand(RunwayThreshold runwayThreshold) {
    this.runwayThreshold = runwayThreshold;
  }

  public RunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  @Override
  public String toString(){
    String ret = "Cleared for takeoff {command}";

    return ret;
  }
  
}
