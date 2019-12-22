package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.global.Restriction;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.SetAltitudeRestriction;

public class SetAltitudeRestrictionApplication extends CommandApplication<SetAltitudeRestriction> {

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
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, SetAltitudeRestriction c) {
    IFromAirplane ret;

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
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, SetAltitudeRestriction c) {
    plane.setAltitudeRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
