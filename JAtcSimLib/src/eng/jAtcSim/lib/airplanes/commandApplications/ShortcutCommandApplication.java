package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.rejections.ShortCutToFixNotOnRoute;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ShortcutCommand;

public class ShortcutCommandApplication extends CommandApplication<ShortcutCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ShortcutCommand c) {
    IFromAirplane ret;
    ret = super.checkValidState(plane,c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    int pointIndex = plane.getPilot().getIndexOfNavaidInCommands(c.getNavaid());
    if (pointIndex < 0) {
      ret = new ShortCutToFixNotOnRoute(c);
      return ret;
    }

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ShortcutCommand c) {
    int pointIndex = plane.getPilot().getIndexOfNavaidInCommands(c.getNavaid());
    // hold abort only if fix was found
    if (plane.getState() == Airplane.State.holding) {
      plane.getPilot().abortHolding();
    }

    plane.getPilot().removeAllItemsInQueueUntilIndex(pointIndex);

    return ApplicationResult.getEmpty();
  }


}
