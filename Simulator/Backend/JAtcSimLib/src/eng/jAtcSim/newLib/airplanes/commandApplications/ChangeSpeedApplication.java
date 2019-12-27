package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.eSystem.geo.Coordinates;
import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.global.Restriction;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ChangeSpeedCommand;

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
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, ChangeSpeedCommand c) {
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
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, ChangeSpeedCommand c) {
    if (c.isResumeOwnSpeed()) {
      plane.setSpeedRestriction(null);
    } else {
      plane.setSpeedRestriction(c.getSpeedRestriction());
    }
    return ApplicationResult.getEmpty();
  }
}