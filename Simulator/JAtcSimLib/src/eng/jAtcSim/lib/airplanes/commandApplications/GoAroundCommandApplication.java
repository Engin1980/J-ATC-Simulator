package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;

public class GoAroundCommandApplication extends CommandApplication<GoAroundCommand>{

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow,
        Airplane.State.arrivingCloseFaf,
        Airplane.State.landed,
        Airplane.State.holding
    };
  }

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, GoAroundCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, GoAroundCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getPilot().processOrderedGoAround();

    return ret;
  }
}
