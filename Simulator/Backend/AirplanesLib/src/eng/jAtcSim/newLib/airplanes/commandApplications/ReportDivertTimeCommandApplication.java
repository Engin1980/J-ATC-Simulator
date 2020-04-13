package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ReportDivertTimeCommand;

public class ReportDivertTimeCommandApplication extends CommandApplication<ReportDivertTimeCommand> {

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand pilot, ReportDivertTimeCommand c) {
    pilot.reportDivertTimeLeft();
    ApplicationResult ret = ApplicationResult.getEmpty();
    return ret;
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand pilot, ReportDivertTimeCommand c) {
    Rejection ret;

    if (pilot.isDeparture())
      ret = new Rejection("We do not have divert time as we are a departure.", c);
    else if (pilot.getState().is(Airplane.State.longFinal, Airplane.State.shortFinal)) {
      ret = new Rejection("We cannot report divert time at this moment.", c);
    } else {
      ret = null;
    }

    return ret;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[0];
  }
}
