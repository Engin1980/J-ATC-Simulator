package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.AltitudeRestrictionCommand;

public class AltitudeRestrictionApplication extends CommandApplication<AltitudeRestrictionCommand> {

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
  protected Rejection checkCommandSanity(IAirplaneCommand plane, AltitudeRestrictionCommand c) {
    Rejection ret;

    if (c.getRestriction() != null &&
        (c.getRestriction().direction == AboveBelowExactly.above ||
            c.getRestriction().direction == AboveBelowExactly.exactly) &&
     c.getRestriction().value > plane.getType().maxAltitude){
      ret = new Rejection("Ordered minimal altitude limit is higher than our maximal altitude.", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, AltitudeRestrictionCommand c) {
    plane.setAltitudeRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
