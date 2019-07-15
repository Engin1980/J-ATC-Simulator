package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
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
  protected IFromAirplane checkCommandSanity(Pilot.Pilot5Command pilot, ProceedDirectCommand c) {
    IFromAirplane ret = null;

    if (c.getNavaid() == null)
      ret = new Rejection("Unknown point.", c);

    return ret;

  }

  @Override
  protected ApplicationResult adjustAirplane(Pilot.Pilot5Command pilot, ProceedDirectCommand c) {
    if (pilot.getPlane().getState() == Airplane.State.holding) {
      pilot.abortHolding();
    }

    pilot.setTargetCoordinate(c.getNavaid());
    return ApplicationResult.getEmpty();
  }
}
