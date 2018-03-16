package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.HighOrderedSpeedForApproach;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.UnableToEnterApproachFromDifficultPosition;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToApproachCommand;

public class ClearedToApproachApplication extends CommandApplication<ClearedToApproachCommand> {

  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ClearedToApproachCommand c) {

    IFromAirplane ret = null;

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


    final int MAXIMAL_DISTANCE_TO_ENTER_APPROACH_IN_NM = 17;
    final int MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES = 30;

    // zatim resim jen pozici letadla
    int currentHeading
        = (int) Coordinates.getBearing(plane.getCoordinate(), c.getApproach().getPoint());
    int dist
        = (int) Coordinates.getDistanceInNM(c.getApproach().getPoint(), plane.getCoordinate());
    double minHeading = Headings.add(
        c.getApproach().getRadial(),
        -MAXIMAL_ONE_SIDE_ARC_FROM_APPROACH_RADIAL_TO_ENTER_APPROACH_IN_DEGREES);
    double maxHeading = Headings.add(
        c.getApproach().getRadial(),
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

    plane.getPilot().setApproachBehavior(c.getApproach());

    return ret;
  }
}
