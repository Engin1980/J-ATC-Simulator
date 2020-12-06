package eng.jAtcSim.newLib.airplanes;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;

public enum AirplaneState {

  /**
   * On arrival above FL100
   */
  arrivingHigh,
  /**
   * On arrival below FL100
   */
  arrivingLow,
  /**
   * On arrival < 15nm to FAF
   */
  arrivingCloseFaf,
  /**
   * When cleared to approach flying from IAF to FAF
   */
  flyingIaf2Faf,
  /**
   * Entering approach, before descend
   */
  approachEntry,
  /**
   * Descending in approach
   */
  approachDescend,
  /**
   * Long final on approach
   */
  longFinal,
  /**
   * Short final on approach
   */
  shortFinal,
  /**
   * Landed, breaking to zero
   */
  landed,

  /**
   * Waiting for take-off clearance
   */
  holdingPoint,
  /**
   * Taking off roll on the ground
   */
  takeOffRoll,
  /**
   * Take-off airborne or go-around until acceleration altitude
   */
  takeOffGoAround,
  /**
   * Departure below FL100
   */
  departingLow,
  /**
   * Departure above FL100
   */
  departingHigh,
  /**
   * In hold
   */
  holding;

  public static AirplaneState[] valuesExcept(AirplaneState... exceptions) {
    IList<AirplaneState> ret = new EList<>(AirplaneState.values());
    IList<AirplaneState> exs = new EList<>(exceptions);
    ret.removeMany(exs);
    return ret.toArray(AirplaneState.class);
  }

  public boolean is(AirplaneState... values) {
    boolean ret = false;
    for (AirplaneState value : values) {
      if (this == value) {
        ret = true;
        break;
      }
    }
    return ret;
  }

  public boolean isOnGround() {
    return this == takeOffRoll || this == landed || this == holdingPoint;
  }
}
