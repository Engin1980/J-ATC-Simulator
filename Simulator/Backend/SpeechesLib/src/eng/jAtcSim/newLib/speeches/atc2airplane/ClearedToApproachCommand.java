/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.newLib.speeches.atc2airplane;

import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.ICommand;

;

/**
 *
 * @author Marek
 */
public class ClearedToApproachCommand implements ICommand {
  private String thresholdName;
  private ApproachType type;

  public ClearedToApproachCommand(String thresholdName, ApproachType type) {
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

  public ApproachType getType() {
    return type;
  }
}
