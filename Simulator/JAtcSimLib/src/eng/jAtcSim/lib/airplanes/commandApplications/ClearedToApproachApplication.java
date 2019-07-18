package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.world.newApproaches.ApproachEntry;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;
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

import javax.swing.text.StyledEditorKit;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(IPilotWriteSimple pilot, ClearedToApproachCommand c) {
    IFromAirplane ret = null;
    NewApproachInfo nai;

    ActiveRunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), pilot.getPlane().getType().category);
      if (apps.isEmpty())
        ret = new Rejection(
            "Cannot be cleared to approach. There is no approach kind "
                + c.getType() + " for runway " + rt.getName() + " for our plane.", c);
      else {
        nai = tryCreateApproachInfo(apps, pilot);
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

//  private String checkResultToString(IApproachStage.eResult result) {
//    throw new UnsupportedOperationException("Todo");
//  }

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
  protected ApplicationResult adjustAirplane(IPilotWriteSimple pilot, ClearedToApproachCommand c) {
    ApplicationResult ret = new ApplicationResult();

    // abort holding, only if fix was found
    if (pilot.getPlane().getState() == Airplane.State.holding) {
      pilot.getAdvanced().abortHolding();
    }

    Restriction sr = pilot.getPlane().getSha().getSpeedRestriction();

    if (sr != null &&
        (sr.direction == Restriction.eDirection.atLeast ||
            sr.direction == Restriction.eDirection.exactly) &&
        sr.value > pilot.getPlane().getType().vApp) {
      IFromAirplane tmp = new HighOrderedSpeedForApproach(sr.value, pilot.getPlane().getType().vApp);
      ret.informations.add(tmp);
    }

    ActiveRunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    IReadOnlyList<Approach> apps = rt.getApproaches(c.getType(), pilot.getPlane().getType().category);
    NewApproachInfo nai = tryCreateApproachInfo(apps, pilot);
    assert nai != null;

    pilot.getAdvanced().clearedToApproach(nai);

    return ret;
  }

  private NewApproachInfo tryCreateApproachInfo(IReadOnlyList<Approach> apps, IPilotRO pilot) {
    NewApproachInfo ret = null;

    for (Approach approach : apps) {
      if (ret != null) break;
      for (ApproachEntry entry : approach.getEntries()) {
        if (ret != null) break;
        if (entry.getLocation().isInside(pilot.getPlane().getCoordinate())){
          ret = new NewApproachInfo(entry, approach);
        }
      }
    }

    return null;
  }
}
