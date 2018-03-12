package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.DivertCommand;

public class DivertCommandApplication extends CommandApplication<DivertCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, DivertCommand c) {
    IFromAirplane ret = null;

    if (plane.isArrival() == false)
      ret = new Rejection("We are departure, we will not divert.", c);
    else {
      ret = super.checkInvalidState(plane, c,
          Airplane.State.approachEnter,
          Airplane.State.approachDescend,
          Airplane.State.longFinal,
          Airplane.State.shortFinal,
          Airplane.State.landed,
          Airplane.State.takeOffGoAround);
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, DivertCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getPilot().processOrderedDivert();

    return ret;
  }
}
