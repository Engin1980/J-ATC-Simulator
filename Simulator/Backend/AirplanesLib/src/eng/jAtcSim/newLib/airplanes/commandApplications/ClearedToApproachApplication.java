package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.geo.Coordinate;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.INotification;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.HighOrderedSpeedForApproach;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedToApproachCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedToApproachCommand c) {
    ApplicationResult ret = new ApplicationResult();

    Restriction sr = plane.getReader().getSha().getSpeedRestriction();

    if (sr != null &&
        (sr.direction == AboveBelowExactly.above ||
            sr.direction == AboveBelowExactly.exactly) &&
        sr.value > plane.getReader().getType().vApp) {
      INotification tmp = new HighOrderedSpeedForApproach(sr.value, plane.getReader().getType().vApp);
      ret.informations.add(tmp);
    }

    ActiveRunwayThreshold rt = LAcc.getAirport().tryGetRunwayThreshold(c.getThresholdName());
    ApproachInfo ai = ApproachInfo.create(rt, c.getType(), plane.getReader().getType().category, plane.getReader().getCoordinate());
    assert ai.status == ApproachInfo.Status.ok : "Error to obtain approach.";

    plane.getWriter().clearedToApproach(ai.approach, ai.entry);

    return ret;
  }

  @Override
  protected Rejection checkCommandSanity(Airplane plane, ClearedToApproachCommand c) {
    Rejection ret;

    ActiveRunwayThreshold rt = LAcc.getAirport().tryGetRunwayThreshold(c.getThresholdName());
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      ApproachInfo ai = ApproachInfo.create(rt, c.getType(), plane.getReader().getType().category, plane.getReader().getCoordinate());
      switch (ai.status) {
        case noApproachAtAll:
          ret = new Rejection(
              sf("Cannot be cleared to approach. There is no approach for runway %s.",
                  c.getType().toString(), rt.getName()), c);
          break;
        case noApproachKind:
          ret = new Rejection(
              sf("Cannot be cleared to approach. There is no approach kind %s for runway %s.",
                  c.getType().toString(), rt.getName()), c);
          break;
        case noApproachForPlaneType:
          ret = new Rejection(
              sf("Cannot be cleared to approach. There is no approach kind %s for runway %s for our plane type.",
                  c.getType().toString(), rt.getName()), c);
          break;
        case noApproachForPlaneLocation:
          ret = new UnableToEnterApproachFromDifficultPosition(c, "We are not in the correct position to enter the approach.");
          break;
        case ok:
          ret = null;
          break;
        default:
          throw new EEnumValueUnsupportedException(ai.status);
      }
    }
    return ret;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround,
        AirplaneState.departingLow,
        AirplaneState.departingHigh,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }
}

class ApproachInfo {
  public enum Status {
    ok,
    noApproachAtAll,
    noApproachKind,
    noApproachForPlaneType,
    noApproachForPlaneLocation
  }

  public static ApproachInfo create(ActiveRunwayThreshold threshold, ApproachType type,
                                    char planeCategory, Coordinate planeEntryLocation
  ) {
    IReadOnlyList<Approach> apps = threshold.getApproaches();

    // select all approaches
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachAtAll);

    apps = apps.where(q->q.getType() == type);
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachKind);

    // select by type
    apps = apps.where(q -> q.getEntries().isAny(p -> p.isForCategory(planeCategory)));
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachForPlaneType);

    // select by location
    for (Approach app : apps) {
      for (ApproachEntry entry : app.getEntries()) {
        if (entry.getEntryLocation().isInside(planeEntryLocation)) {
          return new ApproachInfo(entry, app);
        }
      }
    }

    return new ApproachInfo(Status.noApproachForPlaneLocation);
  }

  public final Status status;
  public final ApproachEntry entry;
  public final Approach approach;

  private ApproachInfo(ApproachEntry entry, Approach approach) {
    this.status = Status.ok;
    this.entry = entry;
    this.approach = approach;
  }

  private ApproachInfo(Status status) {
    EAssert.Argument.isTrue(status != Status.ok);
    this.status = status;
    this.entry = null;
    this.approach = null;
  }
}