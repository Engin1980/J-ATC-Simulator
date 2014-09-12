/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes.pilots;

import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.ETime;

/**
 *
 * @author Marek
 */
class HoldInfo {
  public enum ePhase{
    beginning,
    entering,
    firstTurn,
    outbound,
    secondTurn,
    inbound,
    parAgainst,
    parTurn
  }
  
  public Coordinate fix;
  public int incomingFixHeading;
  public ePhase phase;
  public ETime secondTurnTime;
  public int outboundHeading;
}
