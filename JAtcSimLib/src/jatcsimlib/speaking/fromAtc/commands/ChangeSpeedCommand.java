/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.speaking.fromAtc.commands;

import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ChangeSpeedCommand implements IAtcCommand {

  private final SpeedRestriction value;

  public ChangeSpeedCommand() {
    value = null;
  }

  public ChangeSpeedCommand(SpeedRestriction.eDirection direction, int speedInKts) {
    SpeedRestriction sr = new SpeedRestriction(direction, speedInKts);
    this.value = sr;
  }

  public ChangeSpeedCommand(SpeedRestriction value) {
    this.value = value;
  }

  public SpeedRestriction.eDirection getDirection() {
    return value.direction;
  }

  public int getSpeedInKts() {
    return value.speedInKts;
  }

  public boolean isResumeOwnSpeed() {
    return value == null;
  }

  @Override
  public String toString() {
    if (isResumeOwnSpeed()) {
      return "Resume own speed {command}";
    } else {
      switch (value.direction){
        case atLeast:
          return "Speed at least " + value.speedInKts + "kts {command}";
        case atMost:
          return "Speed at most " + value.speedInKts + "kts {command}";
        case exactly:
          return "Speed exactly " + value.speedInKts + "kts {command}";
        default:
          throw new ENotSupportedException();
      }
    }
  }

  public SpeedRestriction getSpeedRestriction() {
    return value;
  }
}
