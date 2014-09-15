/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes.pilots;

import jatcsimlib.coordinates.Coordinate;
import jatcsimlib.global.ETime;
import jatcsimlib.global.Headings;

/**
 *
 * @author Marek
 */
class HoldInfo {
  public enum ePhase{
    beginning,
    directEntry,
    parallelEntry,
    parallelAgainst,
    parallelTurn,
    tearEntry,
    tearAgainst,
    firstTurn,
    outbound,
    secondTurn,
    inbound
  }
  
  public Coordinate fix;
  public int inboundRadial;
  public ePhase phase;
  public ETime secondTurnTime;
  public boolean isLeftTurned;
  public int getOutboundHeading(){
    return Headings.add(inboundRadial, 180);
  }
}
