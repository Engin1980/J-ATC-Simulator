package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.Restriction;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.SetAltitudeRestriction;

public class SetAltitudeRestrictionApplication extends CommandApplication<SetAltitudeRestriction> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, SetAltitudeRestriction c) {
    IFromAirplane ret;

    //TODO now changing is not possible for approach
    ret = super.checkInvalidState(plane, c,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    if (c.getRestriction() != null &&
        (c.getRestriction().direction == Restriction.eDirection.atLeast ||
            c.getRestriction().direction == Restriction.eDirection.exactly) &&
     c.getRestriction().value > plane.getType().maxAltitude){
      ret = new Rejection("Ordered minimal altitude limit is higher than our maximal altitude.", c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, SetAltitudeRestriction c) {
    plane.getPilot().setAltitudeRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
