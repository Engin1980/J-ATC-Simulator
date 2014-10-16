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

  public static final int CURRENT_HEADING = -1;

  public enum eDirection {

    any,
    left,
    right
  }

  private final int heading;
  private final eDirection direction;

  public ChangeHeadingCommand() {
    heading = CURRENT_HEADING;
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

  public boolean isCurrentHeading() {
    return heading == CURRENT_HEADING;
  }

  @Override
  public String toString() {
    if (isCurrentHeading()) {
      return "FCH";
    } else {
      return "FH{" + heading + '}';
    }
  }

}
