/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.speaking.commands.specific;

import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.SpeedRestriction;
import jatcsimlib.speaking.commands.Command;

/**
 *
 * @author Marek
 */
public class ChangeSpeedCommand extends Command {

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
      return "SR";
    } else {
      switch (value.direction){
        case atLeast:
          return "SL{" + value.speedInKts + "}";
        case atMost:
          return "SM{" + value.speedInKts + "}";
        case exactly:
          return "SE{" + value.speedInKts + "}";
        default:
          throw new ENotSupportedException();
      }
    }
  }

  public SpeedRestriction getSpeedRestriction() {
    return value;
  }
}
