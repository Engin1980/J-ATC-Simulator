/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.approaches.Approach;

/**
 *
 * @author Marek
 */
public class ClearedToApproachCommand implements IAtcCommand {
  private String thresholdName;
  private Approach.ApproachType type;

  public ClearedToApproachCommand(String thresholdName, Approach.ApproachType type) {
    this.thresholdName = thresholdName;
    this.type = type;
  }

  @Override
  public String toString() {
    return "Cleared for approach " + type + " at " + thresholdName + " {command}";
  }

  public String getThresholdName() {
    return thresholdName;
  }

  public Approach.ApproachType getType() {
    return type;
  }
}
