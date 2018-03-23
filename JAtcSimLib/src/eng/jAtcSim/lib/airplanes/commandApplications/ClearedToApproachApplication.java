package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.HighOrderedSpeedForApproach;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;
import eng.jAtcSim.lib.world.Navaid;
import eng.jAtcSim.lib.world.RunwayThreshold;
import eng.jAtcSim.lib.world.approaches.CurrentApproachInfo;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ClearedToApproachCommand c) {

    IFromAirplane ret;

    ret = super.checkInvalidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    CurrentApproachInfo cai = null;
    if (rt == null) {
      ret = new Rejection(
          "Cannot be cleared to approach. There is no runway designated as " + c.getThresholdName(), c);
    } else {
      cai = rt.tryGetCurrentApproachInfo(c.getType(), plane.getType().category, null);
      if (cai == null) {
        ret = new Rejection(
            "Cannot be cleared to approach. There is no approach type "
                + c.getType() + " for runway " + rt.getName(), c);
      }
    }
    if (ret != null) return ret;


    final int MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM = 17;
    final int MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES = 30;

    // zatim resim jen pozici letadla
    int currentHeading
        = (int) Coordinates.getBearing(plane.getCoordinate(), cai.getMapt());
    int dist
        = (int) Coordinates.getDistanceInNM(cai.getMapt(), plane.getCoordinate());
    double minHeading = Headings.add(
        cai.getCourse(),
        -MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES);
    double maxHeading = Headings.add(
        cai.getCourse(),
        MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES);

    if (dist > MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM ||
        !Headings.isBetween(minHeading, currentHeading, maxHeading)) {
      ret = new UnableToEnterApproachFromDifficultPosition(c);
    }
    if (ret != null) return ret;

    return ret;
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


    Navaid iaf = plane.tryGetIaf();
    RunwayThreshold rt = Acc.airport().tryGetRunwayThreshold(c.getThresholdName());
    CurrentApproachInfo cai = rt.tryGetCurrentApproachInfo(
        c.getType(),
        plane.getType().category,
        iaf);
    assert cai != null;

    plane.getPilot().setApproachBehavior(cai);

    return ret;
  }
}
