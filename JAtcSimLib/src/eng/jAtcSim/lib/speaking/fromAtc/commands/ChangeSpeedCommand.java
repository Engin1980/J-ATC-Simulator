/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ChangeSpeedCommand implements IAtcCommand {

  private final Restriction value;

  public ChangeSpeedCommand() {
    value = null;
  }

  public ChangeSpeedCommand(Restriction.eDirection direction, int speedInKts) {
    Restriction sr = new Restriction(direction, speedInKts);
    this.value = sr;
  }

  public ChangeSpeedCommand(Restriction value) {
    this.value = value;
  }

  public Restriction.eDirection getDirection() {
    return value.direction;
  }

  public int getSpeedInKts() {
    return value.value;
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
          return "Speed at least " + value.value + "kts {command}";
        case atMost:
          return "Speed at most " + value.value + "kts {command}";
        case exactly:
          return "Speed exactly " + value.value + "kts {command}";
        default:
          throw new EEnumValueUnsupportedException(value.direction);
      }
    }
  }

  public Restriction getSpeedRestriction() {
    return value;
  }
}
