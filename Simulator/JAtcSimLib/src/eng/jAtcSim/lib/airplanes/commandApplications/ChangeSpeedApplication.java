package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.forPilot.IPilotWriteSimple;
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
  protected IFromAirplane checkCommandSanity(IPilotWriteSimple pilot, ChangeSpeedCommand c) {
    IFromAirplane ret;

    if (c.isResumeOwnSpeed() == false) {
      // not resume speed

      Restriction sr = c.getSpeedRestriction();
      boolean isInApproach = pilot.getPlane().getState().is(
          Airplane.State.approachEnter,
          Airplane.State.approachDescend
      );

      int cMax = !isInApproach ? pilot.getPlane().getType().vMaxClean : pilot.getPlane().getType().vMaxApp;
      int cMin = !isInApproach ? pilot.getPlane().getType().vMinClean : pilot.getPlane().getType().vMinApp;
      // next "if" allows speed under vMinClean (like flaps-1) near the FAF
      if (!isInApproach && Coordinates.getDistanceInNM(pilot.getPlane().getCoordinate(), Acc.airport().getLocation()) < 20) {
        //cMin = (int) (cMin * 0.85);
        cMin = pilot.getPlane().getType().vMaxApp;
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
  protected ApplicationResult adjustAirplane(IPilotWriteSimple pilot, ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed()) {
      pilot.setSpeedRestriction(null);
    } else {
      pilot.setSpeedRestriction(c.getSpeedRestriction());
    }
    return ApplicationResult.getEmpty();
  }
}
