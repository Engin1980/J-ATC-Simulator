package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.pilots.updaters.ToCoordinateHeadingUpdater;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ProceedDirectCommand;

public class ProceedDirectApplication extends CommandApplication<ProceedDirectCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
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
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ProceedDirectCommand c) {
    Rejection ret = null;

    if (Gimme.tryGetNavaid(c.getNavaidName()) == null)
      ret = new Rejection("Unknown point.", c);

    return ret;

  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ProceedDirectCommand c) {
    if (plane.getState() == Airplane.State.holding) {
      plane.abortHolding();
    }

    Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
    plane.setTargetHeading(
        new ToCoordinateHeadingUpdater
    )

    plane.setNavigator(
        new ToCoordinateNavigator(c.getNavaid().getCoordinate()));
    return ApplicationResult.getEmpty();
  }
}
