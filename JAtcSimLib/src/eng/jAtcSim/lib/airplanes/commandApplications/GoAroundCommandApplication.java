package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;

public class GoAroundCommandApplication extends CommandApplication<GoAroundCommand>{
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, GoAroundCommand c) {

    IFromAirplane ret = null;

    ret = super.checkInvalidState(plane, c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.landed);
    if (ret != null) return ret;

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, GoAroundCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getPilot().processOrderedGoAround();

    return ret;
  }
}
