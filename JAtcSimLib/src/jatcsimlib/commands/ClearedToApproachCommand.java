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
  private final Approach.eType type;
  private final String runwayThresholdName;

  public ClearedToApproachCommand(Approach.eType type, String runwayName) {
    this.type = type;
    this.runwayThresholdName = runwayName;
  }

  public Approach.eType getType() {
    return type;
  }

  public String getRunwayThresholdName() {
    return runwayThresholdName;
  }
  
  
}
