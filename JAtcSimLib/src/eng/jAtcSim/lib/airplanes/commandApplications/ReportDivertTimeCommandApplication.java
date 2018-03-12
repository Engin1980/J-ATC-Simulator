package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.DivertTimeNotification;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ReportDivertTime;

public class ReportDivertTimeCommandApplication extends CommandApplication<ReportDivertTime> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ReportDivertTime c) {
    IFromAirplane ret;

    if (plane.isArrival() == false)
      ret = new Rejection("We do not have divert time as we are a departure.", c);
    else if (plane.getState().is(Airplane.State.longFinal, Airplane.State.shortFinal)){
      ret = new Rejection("We cannot report divert time at this moment.", c);
    } else {
      ret = null;
    }

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ReportDivertTime c) {
    ApplicationResult ret = new ApplicationResult();
    ret.rejection = null;
    ret.informations.add(new DivertTimeNotification(plane.getPilot().getDivertMinutesLeft()));
    return ret;
  }
}
