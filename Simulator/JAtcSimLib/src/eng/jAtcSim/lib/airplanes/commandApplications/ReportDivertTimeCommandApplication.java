package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;

public class ReportDivertTimeCommandApplication extends CommandApplication<ReportDivertTime> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[0];
  }

  @Override
  protected IFromAirplane checkCommandSanity(Pilot.Pilot5Command pilot, ReportDivertTime c) {
    IFromAirplane ret;

    if (pilot.getFlight().isArrival() == false)
      ret = new Rejection("We do not have divert time as we are a departure.", c);
    else if (pilot.getPlane().getState().is(Airplane.State.longFinal, Airplane.State.shortFinal)){
      ret = new Rejection("We cannot report divert time at this moment.", c);
    } else {
      ret = null;
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Pilot.Pilot5Command pilot, ReportDivertTime c) {
    ApplicationResult ret = new ApplicationResult();
    ret.rejection = null;
    ret.informations.add(new DivertTimeNotification(pilot.getDivertMinutesLeft()));
    return ret;
  }
}
