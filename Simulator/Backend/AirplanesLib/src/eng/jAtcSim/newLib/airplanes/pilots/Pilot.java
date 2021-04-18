package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneWriter;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.Restriction;
import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class Pilot implements IXPersistable {

  /**
   * This is used to recognize if the airplane is in some approach stage, but for now
   * to high to be really be close to approach. Typical situation is when overflying
   * IAF or runway threshold at high altitude.
   */
  private static final int TO_HIGH_TO_BE_APPROACHING = 7000;
  @XIgnored protected final IAirplane rdr;
  @XIgnored protected final IAirplaneWriter wrt;
  private boolean isFirstElapseSecond = true;

  @XConstructor
  protected Pilot(XLoadContext ctx) {
    this(ctx.getParents().get(Airplane.class));
  }

  public Pilot(Airplane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.rdr = plane.getReader();
    this.wrt = plane.getWriter();
  }

  public abstract boolean isDivertable();

  protected abstract void elapseSecondInternal();

  protected abstract AirplaneState[] getInitialStates();

  protected abstract AirplaneState[] getValidStates();

  public void adjustTargetSpeed() {
    int minOrdered;
    int maxOrdered;
    Restriction speedRestriction = rdr.getSha().getSpeedRestriction();
    if (speedRestriction != null) {
      switch (speedRestriction.direction) {
        case exactly:
          minOrdered = speedRestriction.value;
          maxOrdered = speedRestriction.value;
          break;
        case above:
          minOrdered = speedRestriction.value;
          maxOrdered = Integer.MAX_VALUE;
          break;
        case below:
          minOrdered = Integer.MIN_VALUE;
          maxOrdered = speedRestriction.value;
          break;
        default:
          throw new EEnumValueUnsupportedException(speedRestriction.direction);
      }
    } else {
      minOrdered = Integer.MIN_VALUE;
      maxOrdered = Integer.MAX_VALUE;
    }
    int ts;
    switch (rdr.getState()) {
      case holdingPoint:
      case landed:
        ts = 0;
        break;
      case takeOffRoll:
      case takeOff:
        ts = rdr.getType().vR + 10;
        break;
      case departingLow:
      case arrivingLow:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(250, rdr.getType().vCruise), maxOrdered);
        break;
      case departingHigh:
      case arrivingHigh:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(287, rdr.getType().vCruise), maxOrdered);
        break;
      case arrivingCloseFaf:
      case flyingIaf2Faf:
        if (rdr.getSha().getAltitude() - Context.getArea().getAirport().getAltitude() > TO_HIGH_TO_BE_APPROACHING)
          ts = NumberUtils.boundBetween(minOrdered, Math.min(287, rdr.getType().vCruise), maxOrdered);
        else
          ts = NumberUtils.boundBetween(minOrdered, Math.min(287, rdr.getType().vMinClean + 15), maxOrdered);
        break;
      case approachEntry:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(rdr.getType().vMaxApp, rdr.getType().vMinClean), maxOrdered);
        break;
      case approachDescend:
        ts = NumberUtils.boundBetween(minOrdered, rdr.getType().vApp, maxOrdered);
        break;
      case longFinal:
      case shortFinal:
        minOrdered = Math.max(minOrdered, rdr.getType().vMinApp);
        maxOrdered = Math.min(maxOrdered, rdr.getType().vMaxApp);
        ts = NumberUtils.boundBetween(minOrdered, rdr.getType().vApp, maxOrdered);
        break;
      case holding:
        int holdingOptimalSpeed; // https://www.quora.com/What-is-the-most-efficient-altitude-and-speed-for-a-plane-to-fly-at-when-in-a-holding-pattern
        if (rdr.getSha().getTargetAltitude() > 24000)
          holdingOptimalSpeed = 265;
        else if (rdr.getSha().getTargetAltitude() > 20000)
          holdingOptimalSpeed = 240;
        else if (rdr.getSha().getTargetAltitude() > 10000)
          holdingOptimalSpeed = 230;
        else
          holdingOptimalSpeed = rdr.getType().vMinClean + 10;
        ts = NumberUtils.boundBetween(minOrdered, Math.min(holdingOptimalSpeed, rdr.getType().vCruise), maxOrdered);
        break;
      default:
        throw new EEnumValueUnsupportedException(rdr.getState());
    }

    if (rdr.getSha().getTargetSpeed() != ts)
      wrt.setTargetSpeed(ts);
  }

  public final void elapseSecond() {
    if (isFirstElapseSecond) {
      if (ArrayUtils.contains(getInitialStates(), rdr.getState()) == false)
        throw new EApplicationException(sf(
                "Airplane %s has illegal initial state %s for pilot %s.",
                rdr.getCallsign().toString(), rdr.getState().toString(), this.getClass().getName()
        ));
      isFirstElapseSecond = false;
    } else {
      if (ArrayUtils.contains(getValidStates(), rdr.getState()) == false)
        throw new EApplicationException(sf(
                "Airplane %s has illegal state %s for pilot %s.",
                rdr.getCallsign().toString(), rdr.getState().toString(), this.getClass().getName()
        ));
    }
    elapseSecondInternal();
    adjustTargetSpeed();
  }

  void throwIllegalStateException() {
    throw new ERuntimeException(
            "Illegal state " + rdr.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
