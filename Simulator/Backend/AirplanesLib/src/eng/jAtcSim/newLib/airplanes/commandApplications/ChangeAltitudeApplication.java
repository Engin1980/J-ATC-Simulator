package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
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
  protected PlaneRejection checkCommandSanity(Airplane pilot, ChangeAltitudeCommand c) {
    PlaneRejection ret;

    if ((c.getDirection() == ChangeAltitudeCommand.eDirection.climb) && (pilot.getReader().getSha().getAltitude() > c.getAltitudeInFt())) {
      ret = new PlaneRejection(c,"we are higher");
      if (pilot.getReader().getSha().getTargetAltitude() < c.getAltitudeInFt())
        pilot.getWriter().setTargetAltitude(c.getAltitudeInFt());
      return ret;
    } else if ((c.getDirection() == ChangeAltitudeCommand.eDirection.descend) && (pilot.getReader().getSha().getAltitude() < c.getAltitudeInFt())) {
      if (pilot.getReader().getSha().getTargetAltitude() > c.getAltitudeInFt())
        pilot.getWriter().setTargetAltitude(c.getAltitudeInFt());
      ret = new PlaneRejection(c,"we are lower");
      return ret;
    }

    if (c.getAltitudeInFt() > pilot.getReader().getType().maxAltitude) {
      ret = new PlaneRejection(c,"too high");
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
