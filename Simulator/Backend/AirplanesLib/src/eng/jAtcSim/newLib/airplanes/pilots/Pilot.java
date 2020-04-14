package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.Restriction;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class Pilot {

  protected final IPilotPlane plane;
  private boolean isFirstElapseSecond = true;

  public Pilot(IPilotPlane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.plane = plane;
  }

  public void adjustTargetSpeed() {
    int minOrdered;
    int maxOrdered;
    Restriction speedRestriction = plane.getSpeedRestriction();
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
    switch (plane.getState()) {
      case holdingPoint:
      case landed:
        ts = 0;
        break;
      case takeOffRoll:
      case takeOffGoAround:
        ts = plane.getType().vR + 10;
        break;
      case departingLow:
      case arrivingLow:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(250, plane.getType().vCruise), maxOrdered);
        break;
      case departingHigh:
      case arrivingHigh:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(287, plane.getType().vCruise), maxOrdered);
        break;
      case arrivingCloseFaf:
      case flyingIaf2Faf:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(287, plane.getType().vMinClean + 15), maxOrdered);
        break;
      case approachEnter:
        ts = NumberUtils.boundBetween(minOrdered, Math.min(plane.getType().vMaxApp, plane.getType().vMinClean), maxOrdered);
        break;
      case approachDescend:
        ts = NumberUtils.boundBetween(minOrdered, plane.getType().vApp, maxOrdered);
        break;
      case longFinal:
      case shortFinal:
        minOrdered = Math.max(minOrdered, plane.getType().vMinApp);
        maxOrdered = Math.min(maxOrdered, plane.getType().vMaxApp);
        ts = NumberUtils.boundBetween(minOrdered, plane.getType().vApp, maxOrdered);
        break;
      case holding:
        if (plane.getTargetAltitude() > 10000)
          ts = NumberUtils.boundBetween(minOrdered, Math.min(250, plane.getType().vCruise), maxOrdered);
        else
          ts = NumberUtils.boundBetween(minOrdered, Math.min(220, plane.getType().vCruise), maxOrdered);
        break;
      default:
        throw new EEnumValueUnsupportedException(plane.getState());
    }
    plane.setTargetSpeed(ts);
  }

  public final void elapseSecond() {
    if (isFirstElapseSecond) {
      if (ArrayUtils.contains(getInitialStates(), plane.getState()) == false)
        throw new EApplicationException(sf(
            "Airplane %s has illegal initial state %s for pilot %s.",
            plane.getCallsign().toString(), plane.getState().toString(), this.getClass().getName()
        ));
      isFirstElapseSecond = false;
    } else {
      if (ArrayUtils.contains(getValidStates(), plane.getState()) == false)
        throw new EApplicationException(sf(
            "Airplane %s has illegal state %s for pilot %s.",
            plane.getCallsign().toString(), plane.getState().toString(), this.getClass().getName()
        ));
    }
    elapseSecondInternal();
  }

  public abstract boolean isDivertable();

  protected abstract void elapseSecondInternal();

  protected abstract Airplane.State[] getInitialStates();

  protected abstract Airplane.State[] getValidStates();

  void throwIllegalStateException() {
    throw new ERuntimeException(
        "Illegal state " + plane.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
