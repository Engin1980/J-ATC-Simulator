package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.shared.enums.AboveBelowExactly;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.AltitudeRestrictionCommand;
import eng.jAtcSim.newLib.speeches.base.Rejection;

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
  protected PlaneRejection checkCommandSanity(Airplane plane, AltitudeRestrictionCommand c) {
    PlaneRejection ret;

    if (c.isClearRestriction() == false &&
        (c.getRestriction().direction == AboveBelowExactly.above ||
            c.getRestriction().direction == AboveBelowExactly.exactly) &&
     c.getRestriction().value > plane.getReader().getType().maxAltitude){
      ret = new PlaneRejection(c,"Ordered minimal altitude limit is higher than our maximal altitude.");
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, AltitudeRestrictionCommand c) {
    if (c.isClearRestriction())
      plane.getWriter().setAltitudeRestriction(null);
    else
      plane.getWriter().setAltitudeRestriction(c.getRestriction());
    return ApplicationResult.getEmpty();
  }
}
