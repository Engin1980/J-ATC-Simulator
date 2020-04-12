package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeAltitudeCommand;

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
  protected Rejection checkCommandSanity(IAirplaneCommand pilot, ChangeAltitudeCommand c) {
    Rejection ret;

    if ((c.getDirection() == ChangeAltitudeCommand.eDirection.climb) && (pilot.getAltitude() > c.getAltitudeInFt())) {
      ret = new Rejection("we are higher", c);
      if (pilot.getTargetAltitude() < c.getAltitudeInFt())
        pilot.setTargetAltitude(c.getAltitudeInFt());
      return ret;
    } else if ((c.getDirection() == ChangeAltitudeCommand.eDirection.descend) && (pilot.getAltitude() < c.getAltitudeInFt())) {
      if (pilot.getTargetAltitude() > c.getAltitudeInFt())
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
  protected ApplicationResult adjustAirplane(IAirplaneCommand  pilot, ChangeAltitudeCommand c) {
    pilot.setTargetAltitude(c.getAltitudeInFt());
    return ApplicationResult.getEmpty();
  }
}
