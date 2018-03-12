package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;

public class HoldCommandApplication extends CommandApplication<HoldCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, HoldCommand c) {
    IFromAirplane ret;
    ret = super.checkInvalidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, HoldCommand c) {
    plane.getPilot().setTargetCoordinate(null);

    plane.getPilot().setHoldBehavior(c.getNavaid().getCoordinate(), c.getInboundRadial(), c.isLeftTurn());

    return ApplicationResult.getEmpty();
  }
}
