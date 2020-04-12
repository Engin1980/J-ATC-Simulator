package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.DivertCommand;

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
