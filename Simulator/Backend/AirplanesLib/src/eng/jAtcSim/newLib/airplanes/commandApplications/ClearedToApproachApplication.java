package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.collections.ESet;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.collections.ISet;
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
import eng.jAtcSim.newLib.area.approaches.ApproachEntryCondition;
import eng.jAtcSim.newLib.shared.Restriction;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.HighOrderedSpeedForApproach;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToApproachCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

interface IApproachInfo {
}

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
    IApproachInfo ai = ApproachInfoFactory.create(rt, c.getType(), plane.getReader());
    assert ai instanceof AcceptedApproachInfo : "Error to obtain approach.";
    AcceptedApproachInfo aai = (AcceptedApproachInfo) ai;
    plane.getWriter().clearedToApproach(aai.approach, aai.entry);

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
      IApproachInfo ai = ApproachInfoFactory.create(rt, c.getType(), plane.getReader());
      if (ai instanceof AcceptedApproachInfo)
        ret = null;
      else {
        RejectedApproachInfo rai = (RejectedApproachInfo) ai;
        switch (rai.reason) {
          case thresholdNotInSight:
            ret = new PlaneRejection(c,
                    sf("Cannot be cleared to approach. Runway %s not in sight.",
                            rt.getName()));
            break;
          case noApproachAtAll:
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
          case invalidAltitude:
            ret = new UnableToEnterApproachFromDifficultPosition(c, "We are too high.");
            break;
          case invalidHeading:
            ret = new UnableToEnterApproachFromDifficultPosition(c, "We are heading wrong direction.");
            break;
          case invalidLocation:
            ret = new UnableToEnterApproachFromDifficultPosition(c,
                    sf("We are not in the correct position to enter %s approach.", c.getType().toString()));
            break;
          default:
            throw new EEnumValueUnsupportedException(rai.reason);
        }
      }
    }
    return ret;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
            AirplaneState.holdingPoint,
            AirplaneState.takeOffRoll,
            AirplaneState.takeOff,
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

class AcceptedApproachInfo implements IApproachInfo {
  public final Approach approach;
  public final ApproachEntry entry;

  public AcceptedApproachInfo(ApproachEntry entry, Approach approach) {
    EAssert.Argument.isNotNull(entry, "entry");
    EAssert.Argument.isNotNull(approach, "approach");

    this.entry = entry;
    this.approach = approach;
  }
}

class RejectedApproachInfo implements IApproachInfo {
  public enum RejectionReason {
    noApproachKind,
    noApproachForPlaneType,
    noApproachAtAll,
    invalidAltitude,
    invalidHeading,
    thresholdNotInSight, invalidLocation
  }

  public final RejectionReason reason;

  public RejectedApproachInfo(RejectionReason reason) {
    this.reason = reason;
  }
}

class ApproachInfoFactory {
  public static IApproachInfo create(ActiveRunwayThreshold threshold, ApproachType type,
                                     IAirplane airplane
  ) {
    IReadOnlyList<Approach> apps = threshold.getApproaches();

    // select all approaches
    if (apps.isEmpty())
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.noApproachAtAll);

    apps = apps.where(q -> q.getType() == type);
    if (apps.isEmpty())
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.noApproachKind);

    // select by type
    apps = apps.where(q -> q.getEntries().isAny(p -> p.isForCategory(airplane.getType().category)));
    if (apps.isEmpty())
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.noApproachForPlaneType);

    EAssert.isTrue(apps.size() <= 1, "Here it should be zero or one approach available only.");

    Approach app = apps.getFirst();
    ISet<ApproachEntryCondition.ApproachRejectionReason> rejections = new ESet<>();
    for (ApproachEntry entry : app.getEntries()) {
      IMap<ApproachEntryCondition, Boolean> entryResults = entry
              .getEntryConditions()
              .toMap(q -> ConditionEvaluator.check(q.getEntryCondition(), airplane));
      if (entryResults.getValues().isAll(q -> q))
        return new AcceptedApproachInfo(entry, app);
      else {
        ISet<ApproachEntryCondition.ApproachRejectionReason> rejectionReasons = entryResults
                .where(q -> q.getValue() == false)
                .toSet(q -> q.getKey().getRejectionReason());
        if (rejectionReasons.contains(ApproachEntryCondition.ApproachRejectionReason.invalidLocation) == false)
          rejections.addMany(rejectionReasons);
        else
          rejections.add(ApproachEntryCondition.ApproachRejectionReason.invalidLocation);
      }
    }

    EAssert.isFalse(rejections.isEmpty());
    return convertApproachRejectReasonToApproachInfo(rejections);
  }

  private static IApproachInfo convertApproachRejectReasonToApproachInfo(ISet<ApproachEntryCondition.ApproachRejectionReason> rejections) {
    if (rejections.contains(ApproachEntryCondition.ApproachRejectionReason.thresholdNotInSight))
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.thresholdNotInSight);
    if (rejections.contains(ApproachEntryCondition.ApproachRejectionReason.invalidAltitude))
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.invalidAltitude);
    else if (rejections.contains(ApproachEntryCondition.ApproachRejectionReason.invalidHeading))
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.invalidHeading);
    else
      return new RejectedApproachInfo(RejectedApproachInfo.RejectionReason.invalidLocation);
  }
}

