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
public class ChangeAltitudeCommand extends Command {

  public enum eDirection{
    any,
    climb,
    descend
  
  }
  
  private final eDirection direction;
  private final int altitudeInFt;

  public ChangeAltitudeCommand() {
    direction = eDirection.any;
    altitudeInFt = 0;
  }

  public ChangeAltitudeCommand(eDirection direction, int altitudeInFt) {
    this.direction = direction;
    this.altitudeInFt = altitudeInFt;
  }
}
