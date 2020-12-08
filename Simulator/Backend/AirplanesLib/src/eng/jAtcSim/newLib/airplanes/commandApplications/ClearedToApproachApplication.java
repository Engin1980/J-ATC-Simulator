package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.IAirplane;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.pilots.ConditionEvaluator;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.approaches.Approach;
import eng.jAtcSim.newLib.area.approaches.ApproachEntry;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.HighOrderedSpeedForApproach;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToApproachCommand;

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
      IFromPlaneSpeech tmp = new HighOrderedSpeedForApproach(sr.value, plane.getReader().getType().vApp);
      ret.informations.add(tmp);
    }

    ActiveRunwayThreshold rt = Context.getArea().getAirport().tryGetRunwayThreshold(c.getThresholdName());
    ApproachInfo ai = ApproachInfo.create(rt, c.getType(), plane.getReader());
    assert ai.status == ApproachInfo.Status.ok : "Error to obtain approach.";

    plane.getWriter().clearedToApproach(ai.approach, ai.entry);

    return ret;
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ClearedToApproachCommand c) {
    PlaneRejection ret;

    ActiveRunwayThreshold rt = Context.getArea().getAirport().tryGetRunwayThreshold(c.getThresholdName());
    if (rt == null) {
      ret = new PlaneRejection(c,
              "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName());
    } else {
      ApproachInfo ai = ApproachInfo.create(rt, c.getType(), plane.getReader());
      switch (ai.status) {
        case noApproachAtAll:
          ret = new PlaneRejection(c,
                  sf("Cannot be cleared to approach. There is no approach for runway %s.",
                          c.getType().toString(), rt.getName()));
          break;
        case noApproachKind:
          ret = new PlaneRejection(c,
                  sf("Cannot be cleared to approach. There is no %s approach for runway %s.",
                          c.getType().toString(), rt.getName()));
          break;
        case noApproachForPlaneType:
          ret = new PlaneRejection(c,
                  sf("Cannot be cleared to approach. There is no %s approach for runway %s for our plane type.",
                          c.getType().toString(), rt.getName()));
          break;
        case noApproachForPlaneLocation:
          ret = new UnableToEnterApproachFromDifficultPosition(c, sf("We are not in the correct position to enter %s approach.", c.getType().toString()));
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
            AirplaneState.approachEntry,
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
                                    IAirplane airplane
  ) {
    IReadOnlyList<Approach> apps = threshold.getApproaches();

    // select all approaches
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachAtAll);

    apps = apps.where(q -> q.getType() == type);
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachKind);

    // select by type
    apps = apps.where(q -> q.getEntries().isAny(p -> p.isForCategory(airplane.getType().category)));
    if (apps.isEmpty())
      return new ApproachInfo(Status.noApproachForPlaneType);

    // select by location
    for (Approach app : apps) {
      for (ApproachEntry entry : app.getEntries()) {
        if (ConditionEvaluator.check(entry.getEntryCondition(), airplane)) {
          return new ApproachInfo(entry, app);
        }
      }
    }

    return new ApproachInfo(Status.noApproachForPlaneLocation);
  }

  public final Approach approach;
  public final ApproachEntry entry;
  public final Status status;

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
