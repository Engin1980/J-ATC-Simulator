/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jatcsimlib.airplanes.pilots;

import jatcsimlib.Acc;
import jatcsimlib.world.Approach;

/**
 *
 * @author Marek
 */
class ApproachInfo {
  public enum ePhase {
  approaching,
  finalEnter,
  finalOther,
  shortFinal,
  touchdownAndLanded
  }
  public Approach approach;
  public ePhase phase = ePhase.approaching;
  public final int finalAltitude;
  public final int shortFinalAltitude;
  public boolean isAppSpeedSet;
  public Boolean isRunwayVisible = null;

  public ApproachInfo(Approach approach) {
    this.approach = approach;
    this.finalAltitude = Acc.airport().getAltitude() + 500;
    this.shortFinalAltitude = this.finalAltitude - 300;
    this.isAppSpeedSet = false;
  }
  
}
