package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.CheckApproachStage;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.HighOrderedSpeedForApproach;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.newApproaches.Approach;
import eng.jAtcSim.lib.world.newApproaches.IafRoute;
import eng.jAtcSim.lib.world.newApproaches.NewApproachInfo;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ClearedToApproachCommand c) {

    IFromAirplane ret = null;

    NewApproachInfo nai = null;
    ActiveRunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), plane.getType().category);
      if (apps.isEmpty())
        ret = new Rejection(
            "Cannot be cleared to approach. There is no approach kind "
                + c.getType() + " for runway " + rt.getName() + " for our plane.", c);
      else {
        nai = tryCreateApproachInfo(apps, plane);
        if (nai == null)
          ret = new UnableToEnterApproachFromDifficultPosition(c, "We are not in the correct position to enter the approach.");
      }
    }
    if (ret != null) return ret;

    //TODO here I should check if the approach can be started, but don't know
    //TODO how to pass an instance to for checking
//    for (IApproachStage stage : nai.getStages()) {
//      if (stage instanceof CheckApproachStage) {
//        CheckApproachStage.eCheckResult result = ((CheckApproachStage) stage).check(plane.);
//        if (result != CheckApproachStage.eCheckResult.ok) {
//          ret = new UnableToEnterApproachFromDifficultPosition(c, checkResultToString(result));
//          break;
//        }
//      } else break;
//    }

    throw new UnsupportedOperationException("Return here Null or \"Confirmation\"?");
//    if (ret == null)
//      ret = new Confirmation(c);
//
//    return ret;
  }

  private String checkResultToString(CheckApproachStage.eCheckResult result) {
    throw new UnsupportedOperationException("Todo");
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ClearedToApproachCommand c) {
    ApplicationResult ret = new ApplicationResult();

    // hold abort only if fix was found
    if (plane.getState() == Airplane.State.holding) {
      plane.getPilot().abortHolding();
    }

    Restriction sr = plane.getPilot().getSpeedRestriction();

    if (sr != null &&
        (sr.direction == Restriction.eDirection.atLeast ||
            sr.direction == Restriction.eDirection.exactly) &&
        sr.value > plane.getType().vApp) {
      IFromAirplane tmp = new HighOrderedSpeedForApproach(sr.value, plane.getType().vApp);
      ret.informations.add(tmp);
    }

    ActiveRunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), plane.getType().category);
    NewApproachInfo nai = tryCreateApproachInfo(apps, plane);
    assert nai != null;

    plane.getPilot().setApproachBehavior(nai);

    return ret;
  }

  private NewApproachInfo tryCreateApproachInfo(IReadOnlyList<Approach> apps, Airplane.Airplane4Command plane) {
    Approach app;

    app = apps.tryGetFirst(q -> q.getEntryLocation().isInside(plane.getCoordinate()));
    if (app != null)
      return new NewApproachInfo(app);

    for (Approach approach : apps) {
      for (IafRoute iafRoute : approach.getIafRoutes()) {
        if (Coordinates.getDistanceInNM(plane.getCoordinate(), iafRoute.getNavaid().getCoordinate()) < 2.5) {
          return new NewApproachInfo(approach, iafRoute);
        }
      }
    }

    return null;
  }
}
