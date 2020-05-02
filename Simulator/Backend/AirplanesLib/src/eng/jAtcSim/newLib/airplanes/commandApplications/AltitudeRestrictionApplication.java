package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.AltitudeRestrictionCommand;

public class AltitudeRestrictionApplication extends CommandApplication<AltitudeRestrictionCommand> {

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
  protected Rejection checkCommandSanity(Airplane plane, AltitudeRestrictionCommand c) {
    Rejection ret;

    if (c.getRestriction() != null &&
        (c.getRestriction().direction == AboveBelowExactly.above ||
            c.getRestriction().direction == AboveBelowExactly.exactly) &&
     c.getRestriction().value > plane.getReader().getType().maxAltitude){
      ret = new Rejection("Ordered minimal altitude limit is higher than our maximal altitude.", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, AltitudeRestrictionCommand c) {
    plane.getWriter().setAltitudeRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
