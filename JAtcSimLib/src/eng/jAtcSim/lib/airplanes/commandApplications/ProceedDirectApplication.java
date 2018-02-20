package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;

public class ProceedDirectApplication extends CommandApplication<ProceedDirectCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ProceedDirectCommand c) {
    IFromAirplane ret = null;

    // check state ok
    ret = super.checkValidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
    if (ret != null) return ret;

    // check navaid ok
    if (c.getNavaid() == null)
      ret = new Rejection("Unknown point.", c);
    if (ret != null) return ret;

    return ret;

  }

  @Override
  protected ConfirmationResult adjustAirplane(Airplane.Airplane4Command plane, ProceedDirectCommand c) {
    if (plane.getState() == Airplane.State.holding) {
      plane.getPilot().abortHolding();
    }

    plane.getPilot().setTargetCoordinate(c.getNavaid().getCoordinate());
    return new ConfirmationResult();
  }
}
