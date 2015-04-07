
package jatcsimlib.airplanes.pilots;

import jatcsimlib.Acc;
import jatcsimlib.world.Approach;

/**
 * Only supporting function for Pilot class. Not used outside.
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
