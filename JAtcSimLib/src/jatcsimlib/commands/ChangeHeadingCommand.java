/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jatcsimlib.commands;

/**
 *
 * @author Marek
 */
public class ChangeHeadingCommand extends Command {

  public enum eDirection{
    any,
    left,
    right
  }
  
  private final int heading;
  private final eDirection direction;

  public ChangeHeadingCommand() {
    heading = -1;
    direction = eDirection.any;
  }

  public ChangeHeadingCommand(int heading, eDirection direction) {
    if (heading != 360) {
      heading = heading % 360;
    }
    this.heading = heading;
    this.direction = direction;
  }

  public int getHeading() {
    return heading;
  }

  public eDirection getDirection() {
    return direction;
  }
  
    @Override
  public String toString() {
    return "FH{"+ heading + '}';
  }
  
}
