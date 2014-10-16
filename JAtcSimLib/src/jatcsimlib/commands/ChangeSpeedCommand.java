/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

import jatcsimlib.exceptions.ERuntimeException;

/**
 *
 * @author Marek
 */
public class ChangeSpeedCommand extends Command {

  private static final int RESUME_OWN_SPEED = -1;

  public enum eDirection {

    increase,
    decrease
  }

  private final eDirection direction;
  private final int speedInKts;

  public ChangeSpeedCommand() {
    speedInKts = RESUME_OWN_SPEED;
    direction = eDirection.increase;
  }

  public ChangeSpeedCommand(eDirection direction, int speedInKts) {
    this.direction = direction;
    this.speedInKts = speedInKts;
  }

  public eDirection getDirection() {
    return direction;
  }

  public int getSpeedInKts() {
    return speedInKts;
  }

  public boolean isResumeOwnSpeed() {
    return speedInKts == RESUME_OWN_SPEED;
  }

  @Override
  public String toString() {
    if (isResumeOwnSpeed()) {
      return "SR";
    } else {
      return "SU/SD{" + speedInKts + '}';
    }
  }
}
