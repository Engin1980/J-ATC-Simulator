package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.responses.ShortCutToFixNotOnRouteRejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ShortcutCommand;

public class ShortcutCommandApplication extends CommandApplication<ShortcutCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ShortcutCommand c) {
    Rejection ret = null;

    Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
    if (n == null)
      return super.getIllegalNavaidRejection(c.getNavaidName());

    if (!plane.isGoingToFlightOverNavaid(n) == false) {
      ret = new ShortCutToFixNotOnRouteRejection(c);
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ShortcutCommand c) {
    // hold abort only if fix was found
    if (plane.getState() == Airplane.State.holding) {
      plane.abortHolding();
    }

    Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
    plane.applyShortcut(n);

    return ApplicationResult.getEmpty();
  }


}
