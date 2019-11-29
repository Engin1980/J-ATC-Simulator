package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;

public class DivertCommandApplication extends CommandApplication<DivertCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, DivertCommand c) {
    IFromAirplane ret = null;

    if (plane.getFlightModule().isArrival() == false)
      ret = new Rejection("We are departing, we will not divert.", c);

    return ret;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.takeOffGoAround
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, DivertCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getAdvanced().divert(true);

    return ret;
  }
}
