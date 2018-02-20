package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;

public class ChangeAltitudeApplication extends CommandApplication<ChangeAltitudeCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ChangeAltitudeCommand c) {
    IFromAirplane ret;

    //TODO now changing is not possible for approach
    ret = super.checkValidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    if ((c.getDirection() == ChangeAltitudeCommand.eDirection.climb) && (plane.getAltitude() > c.getAltitudeInFt())) {
      ret = new Rejection("we are higher.", c);
      return ret;
    } else if ((c.getDirection() == ChangeAltitudeCommand.eDirection.descend) && (plane.getAltitude() < c.getAltitudeInFt())) {
      ret = new Rejection("we are lower.", c);
      return ret;
    }


    if (c.getAltitudeInFt() > plane.getType().maxAltitude) {
      ret = new Rejection("too high.", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ChangeAltitudeCommand c) {
    plane.setTargetAltitude(c.getAltitudeInFt());
    return ApplicationResult.getEmpty();
  }
}
