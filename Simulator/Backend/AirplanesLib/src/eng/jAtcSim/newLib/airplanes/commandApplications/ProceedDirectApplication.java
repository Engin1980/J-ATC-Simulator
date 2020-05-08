package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.ToCoordinateNavigator;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ProceedDirectCommand;

public class ProceedDirectApplication extends CommandApplication<ProceedDirectCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ProceedDirectCommand c) {
    if (plane.getReader().getState() == AirplaneState.holding) {
      plane.getWriter().abortHolding();
    }

    Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
    plane.getWriter().setTargetHeading(new ToCoordinateNavigator(n.getCoordinate()));
    return ApplicationResult.getEmpty();
  }

  @Override
  protected Rejection checkCommandSanity(Airplane plane, ProceedDirectCommand c) {
    Rejection ret = null;

    if (Gimme.tryGetNavaid(c.getNavaidName()) == null)
      ret = new Rejection("Unknown point.", c);

    return ret;

  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }
}
