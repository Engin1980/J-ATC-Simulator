package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.ApproachInfo;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.HighOrderedSpeedForApproach;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.world.RunwayThreshold;
import eng.jAtcSim.lib.world.approaches.Approach;
import eng.jAtcSim.lib.world.approaches.CurrentApproachInfo;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ClearedToApproachCommand c) {

    IFromAirplane ret = null;

    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    ApproachInfo ai = null;
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      ai = rt.tryGetCurrentApproachInfo(c.getType(), plane.getType().category, plane.getCoordinate());
      if (ai == null) {
        ret = new Rejection(
            "Cannot be cleared to approach. There is no approach type "
                + c.getType() + " for runway " + rt.getName(), c);
      }
    }
    if (ret != null) return ret;

    boolean isVisual = ai.getType() == Approach.ApproachType.visual;
    if (isVisual || !ai.isUsingIafRoute()) {

      final int MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM = isVisual ? 12 : 7;
      final int MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES = 30;

      // zatim resim jen pozici letadla
      int currentHeading
          = (int) Coordinates.getBearing(plane.getCoordinate(), ai.getMapt());
      double dist;
      if (isVisual)
        dist = Coordinates.getDistanceInNM(ai.getMapt(), plane.getCoordinate());
      else
        dist = Coordinates.getDistanceInNM(ai.getFaf(), plane.getCoordinate());
      double minHeading = Headings.add(
          ai.getCourse(),
          -MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES);
      double maxHeading = Headings.add(
          ai.getCourse(),
          MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES);

      if (dist > MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM)
        ret = new UnableToEnterApproachFromDifficultPosition(c, "We are too far.");
      else if (!isVisual && !Headings.isBetween(minHeading, currentHeading, maxHeading))
        ret = new UnableToEnterApproachFromDifficultPosition(c, "We need to be heading moreless for the runway.");
      else if (isVisual) {
        // check if ground is visible now
        double alt = plane.getAltitude();
        if (alt > Acc.weather().getCloudBaseInFt())
          if (Acc.weather().getCloudBaseHitProbability() > Acc.rnd().nextDouble())
            ret = new UnableToEnterApproachFromDifficultPosition(c, "We don't have ground in sight.");
      }
    }
    if (ret != null) return ret;

    return ret;
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

    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    ApproachInfo ai = rt.tryGetCurrentApproachInfo(
        c.getType(),
        plane.getType().category,
        plane.getCoordinate());
    assert ai != null;

    plane.getPilot().setApproachBehavior(ai);

    return ret;
  }
}
