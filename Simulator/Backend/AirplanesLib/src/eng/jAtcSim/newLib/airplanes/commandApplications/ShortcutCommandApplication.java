package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.rejections.ShortCutToFixNotOnRoute;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ShortcutCommand;

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
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, ShortcutCommand c) {
    IFromAirplane ret = null;

    if (!plane.getRoutingModule().isGoingToFlightOverNavaid(c.getNavaid())) {
      ret = new ShortCutToFixNotOnRoute(c);
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, ShortcutCommand c) {
    // hold abort only if fix was found
    if (plane.getState() == Airplane.State.holding) {
      plane.getAdvanced().abortHolding();
    }

    plane.applyShortcut(c.getNavaid());

    return ApplicationResult.getEmpty();
  }


}
