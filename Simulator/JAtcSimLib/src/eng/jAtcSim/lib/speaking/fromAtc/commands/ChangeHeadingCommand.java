/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.speaking.fromAtc.commands;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

/**
 *
 * @author Marek
 */
public class ChangeHeadingCommand implements IAtcCommand {

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
      return "Fly current heading {command}";
    } else {
      return "Fly heading " + heading + " {command}";
    }
  }

}
