package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ChangeAltitudeCommand;

public class ChangeAltitudeApplication extends CommandApplication<ChangeAltitudeCommand> {

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.takeOffRoll,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }

  @Override
  protected Rejection checkCommandSanity(Airplane pilot, ChangeAltitudeCommand c) {
    Rejection ret;

    if ((c.getDirection() == ChangeAltitudeCommand.eDirection.climb) && (pilot.getReader().getSha().getAltitude() > c.getAltitudeInFt())) {
      ret = new Rejection("we are higher", c);
      if (pilot.getReader().getSha().getTargetAltitude() < c.getAltitudeInFt())
        pilot.getWriter().setTargetAltitude(c.getAltitudeInFt());
      return ret;
    } else if ((c.getDirection() == ChangeAltitudeCommand.eDirection.descend) && (pilot.getReader().getSha().getAltitude() < c.getAltitudeInFt())) {
      if (pilot.getReader().getSha().getTargetAltitude() > c.getAltitudeInFt())
        pilot.getWriter().setTargetAltitude(c.getAltitudeInFt());
      ret = new Rejection("we are lower", c);
      return ret;
    }

    if (c.getAltitudeInFt() > pilot.getReader().getType().maxAltitude) {
      ret = new Rejection("too high", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ChangeAltitudeCommand c) {
    plane.getWriter().setTargetAltitude(c.getAltitudeInFt());
    return ApplicationResult.getEmpty();
  }
}
