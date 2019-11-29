package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
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
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple pilot, ReportDivertTime c) {
    IFromAirplane ret;

    if (pilot.getFlightModule().isArrival() == false)
      ret = new Rejection("We do not have divert time as we are a departure.", c);
    else if (pilot.getState().is(Airplane.State.longFinal, Airplane.State.shortFinal)){
      ret = new Rejection("We cannot report divert time at this moment.", c);
    } else {
      ret = null;
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple pilot, ReportDivertTime c) {
    ApplicationResult ret = new ApplicationResult();
    ret.rejection = null;
    ret.informations.add(new DivertTimeNotification(pilot.getDivertModule().getMinutesLeft()));
    return ret;
  }
}
