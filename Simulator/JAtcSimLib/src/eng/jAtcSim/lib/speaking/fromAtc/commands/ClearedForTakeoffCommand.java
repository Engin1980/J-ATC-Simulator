/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

/**
 *
 * @author Marek
 */
public class ClearedForTakeoffCommand implements IAtcCommand {
  private final ActiveRunwayThreshold runwayThreshold;

  private ClearedForTakeoffCommand() {
    runwayThreshold = null;
  }

  public ClearedForTakeoffCommand(ActiveRunwayThreshold runwayThreshold) {
    this.runwayThreshold = runwayThreshold;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  @Override
  public String toString(){
    String ret = "Cleared for takeoff {command}";

    return ret;
  }
  
}
