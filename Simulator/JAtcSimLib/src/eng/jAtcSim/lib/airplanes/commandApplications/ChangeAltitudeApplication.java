package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeAltitudeCommand;

public class ChangeAltitudeApplication extends CommandApplication<ChangeAltitudeCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.takeOffRoll,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple pilot, ChangeAltitudeCommand c) {
    IFromAirplane ret;

    if ((c.getDirection() == ChangeAltitudeCommand.eDirection.climb) && (pilot.getSha().getAltitude() > c.getAltitudeInFt())) {
      ret = new Rejection("we are higher", c);
      if (pilot.getSha().getTargetAltitude() < c.getAltitudeInFt())
        pilot.setTargetAltitude(c.getAltitudeInFt());
      return ret;
    } else if ((c.getDirection() == ChangeAltitudeCommand.eDirection.descend) && (pilot.getSha().getAltitude() < c.getAltitudeInFt())) {
      if (pilot.getSha().getTargetAltitude() > c.getAltitudeInFt())
        pilot.setTargetAltitude(c.getAltitudeInFt());
      ret = new Rejection("we are lower", c);
      return ret;
    }

    if (c.getAltitudeInFt() > pilot.getType().maxAltitude) {
      ret = new Rejection("too high", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple  pilot, ChangeAltitudeCommand c) {
    pilot.setTargetAltitude(c.getAltitudeInFt());
    return ApplicationResult.getEmpty();
  }
}
