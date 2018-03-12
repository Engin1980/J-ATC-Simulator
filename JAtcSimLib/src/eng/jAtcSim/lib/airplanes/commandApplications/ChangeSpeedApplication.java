package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.coordinates.Coordinates;
import eng.jAtcSim.lib.global.SpeedRestriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;

public class ChangeSpeedApplication extends CommandApplication<ChangeSpeedCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ChangeSpeedCommand c) {
    IFromAirplane ret;

    ret = super.checkInvalidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;


    if (c.isResumeOwnSpeed() == false) {
      // not resume speed

      SpeedRestriction sr = c.getSpeedRestriction();
      boolean isInApproach = plane.getState().is(
          Airplane.State.approachEnter,
          Airplane.State.approachDescend
      );

      int cMax = !isInApproach ? plane.getType().vMaxClean : plane.getType().vMaxApp;
      int cMin = !isInApproach ? plane.getType().vMinClean : plane.getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(plane.getCoordinate(), Acc.threshold().getFafCross()) < 10) {
        cMin = (int) (cMin * 0.85);
      }

      if (sr.direction != SpeedRestriction.eDirection.atMost && sr.speedInKts > cMax) {
        ret = new Rejection("Unable to reach speed " + c.getSpeedInKts() + " kts, maximum is " + cMax + ".", c);
        return ret;
      } else if (sr.direction != SpeedRestriction.eDirection.atLeast && sr.speedInKts < cMin) {
        ret = new Rejection("Unable to reach speed " + c.getSpeedInKts() + " kts, minimum is " + cMin + ".", c);
        return ret;
      }
    }

    return null;

  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed()) {
      plane.getPilot().setSpeedRestriction(null);
    } else {
      plane.getPilot().setSpeedRestriction(c.getSpeedRestriction());
    }
    return ApplicationResult.getEmpty();
  }
}
