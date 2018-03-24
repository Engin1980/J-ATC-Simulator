package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ProceedDirectCommand;

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
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ProceedDirectCommand c) {
    IFromAirplane ret = null;

    if (c.getNavaid() == null)
      ret = new Rejection("Unknown point.", c);

    return ret;

  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ProceedDirectCommand c) {
    if (plane.getState() == Airplane.State.holding) {
      plane.getPilot().abortHolding();
    }

    plane.getPilot().setTargetCoordinate(c.getNavaid().getCoordinate());
    return ApplicationResult.getEmpty();
  }
}
