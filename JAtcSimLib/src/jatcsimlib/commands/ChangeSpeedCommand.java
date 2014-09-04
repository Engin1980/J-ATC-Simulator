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

  public enum eDirection{
    increase,
    decrease
  }
  
  private eDirection direction;
  private int speedInKts;

  public ChangeSpeedCommand() {
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
  
  
}
