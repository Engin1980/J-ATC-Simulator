package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.internal.InternalAcc;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses.ShortCutToFixNotOnRouteRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ShortcutCommand;

public class ShortcutCommandApplication extends CommandApplication<ShortcutCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ShortcutCommand c) {
    // hold abort only if fix was found
    if (plane.getReader().getState() == AirplaneState.holding) {
      plane.getWriter().abortHolding();
    }

    Navaid n = InternalAcc.tryGetNavaid(c.getNavaidName());
    plane.getWriter().applyShortcut(n);

    return ApplicationResult.getEmpty();
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ShortcutCommand c) {
    PlaneRejection ret = null;

    Navaid n = InternalAcc.tryGetNavaid(c.getNavaidName());
    if (n == null)
      return super.getIllegalNavaidRejection(c.getNavaidName());

    if (!plane.getReader().getRouting().isGoingToFlightOverNavaid(n) == false) {
      ret = new ShortCutToFixNotOnRouteRejection(c);
    }

    return ret;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.approachEntry,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }


}
