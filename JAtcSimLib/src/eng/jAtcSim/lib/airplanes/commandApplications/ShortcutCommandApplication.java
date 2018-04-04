package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.ShortCutToFixNotOnRoute;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ShortcutCommand;

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
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ShortcutCommand c) {
    IFromAirplane ret = null;

    if (!plane.getPilot().isFlyingOverNavaidInFuture(c.getNavaid())) {
      ret = new ShortCutToFixNotOnRoute(c);
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ShortcutCommand c) {
    // hold abort only if fix was found
    if (plane.getState() == Airplane.State.holding) {
      plane.getPilot().abortHolding();
    }

    plane.getPilot().applyShortcut(c.getNavaid());

    return ApplicationResult.getEmpty();
  }


}
