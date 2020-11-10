package eng.jAtcSim.newLib.airplanes.pilots;

import eng.eSystem.collections.IMap;
import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EApplicationException;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.exceptions.ERuntimeException;
import eng.eSystem.utilites.ArrayUtils;
import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.IAirplaneWriter;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSimLib.xmlUtils.XmlLoadUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class Pilot {

  public static Pilot load(XElement element, IMap<String, Object> context) {
    Class<?> type = XmlLoadUtils.Class.loadType(element);
    Pilot ret;

    if (TakeOffPilot.class.equals(type)) {
      ret = TakeOffPilot.load(element, context);
    } else if (ApproachPilot.class.equals(type)) {
      ret = ApproachPilot.load(element, context);
    } else if (HoldingPointPilot.class.equals(type)) {
      ret = HoldingPointPilot.load(element, context);
    } else if (HoldPilot.class.equals(type)) {
      ret = HoldPilot.load(element, context);
    } else if (ArrivalPilot.class.equals(type)) {
      ret = ArrivalPilot.load(element, context);
    } else if (DeparturePilot.class.equals(type)) {
      ret = DeparturePilot.load(element, context);
    } else
      throw new EApplicationException("Unable to load pilot of type " + type.getName());

    XmlLoadUtils.Field.restoreFields(element, ret, "isFirstSecondElapsed");

    return ret;
  }

  protected final IAirplane rdr;
  protected final IAirplaneWriter wrt;
  private boolean isFirstElapseSecond = true;

  public Pilot(Airplane plane) {
    EAssert.Argument.isNotNull(plane, "plane");
    this.rdr = plane.getReader();
    this.wrt = plane.getWriter();
  }

  public final void save(XElement target) {
    XmlSaveUtils.Class.storeType(target, this);
    XmlSaveUtils.Field.storeField(target, this, "isFirstElapseSecond");
    _save(target);
  }

  public abstract boolean isDivertable();

  protected abstract void elapseSecondInternal();

  protected abstract void _save(XElement target);

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
      case takeOffGoAround:
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
        ts = NumberUtils.boundBetween(minOrdered, Math.min(287, rdr.getType().vMinClean + 15), maxOrdered);
        break;
      case approachEnter:
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
        if (rdr.getSha().getTargetAltitude() > 10000)
          ts = NumberUtils.boundBetween(minOrdered, Math.min(250, rdr.getType().vCruise), maxOrdered);
        else
          ts = NumberUtils.boundBetween(minOrdered, Math.min(220, rdr.getType().vCruise), maxOrdered);
        break;
      default:
        throw new EEnumValueUnsupportedException(rdr.getState());
    }
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
  }

  void throwIllegalStateException() {
    throw new ERuntimeException(
            "Illegal state " + rdr.getState() + " for behavior " + this.getClass().getSimpleName() + "."
    );
  }
}
