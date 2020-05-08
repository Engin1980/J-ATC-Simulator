package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ReportDivertTimeCommand;

public class ReportDivertTimeCommandApplication extends CommandApplication<ReportDivertTimeCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane pilot, ReportDivertTimeCommand c) {
    pilot.getWriter().reportDivertTimeLeft();
    ApplicationResult ret = ApplicationResult.getEmpty();
    return ret;
  }

  @Override
  protected Rejection checkCommandSanity(Airplane pilot, ReportDivertTimeCommand c) {
    Rejection ret;

    if (pilot.getReader().isDeparture())
      ret = new Rejection("We do not have divert time as we are a departure.", c);
    else if (pilot.getReader().getState().is(AirplaneState.longFinal, AirplaneState.shortFinal)) {
      ret = new Rejection("We cannot report divert time at this moment.", c);
    } else {
      ret = null;
    }

    return ret;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[0];
  }
}
