package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeSpeedCommand;

public class ChangeSpeedApplication extends CommandApplication<ChangeSpeedCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ChangeSpeedCommand c) {
    IFromAirplane ret;

    if (c.isResumeOwnSpeed() == false) {
      // not resume speed

      Restriction sr = c.getSpeedRestriction();
      boolean isInApproach = plane.getState().is(
          Airplane.State.approachEnter,
          Airplane.State.approachDescend
      );

      int cMax = !isInApproach ? plane.getType().vMaxClean : plane.getType().vMaxApp;
      int cMin = !isInApproach ? plane.getType().vMinClean : plane.getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(plane.getCoordinate(), Acc.airport().getLocation()) < 20) {
        //cMin = (int) (cMin * 0.85);
        cMin = plane.getType().vMaxApp;
      }

      if (sr.direction != Restriction.eDirection.atMost && sr.value > cMax) {
        ret = new Rejection("Unable to reach speed " + c.getSpeedInKts() + " kts, maximum is " + cMax, c);
        return ret;
      } else if (sr.direction != Restriction.eDirection.atLeast && sr.value < cMin) {
        ret = new Rejection("Unable to reach speed " + c.getSpeedInKts() + " kts, minimum is " + cMin , c);
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
