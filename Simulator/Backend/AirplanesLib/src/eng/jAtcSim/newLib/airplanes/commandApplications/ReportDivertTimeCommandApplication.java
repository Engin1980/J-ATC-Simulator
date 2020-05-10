package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ReportDivertTimeCommand;

public class ReportDivertTimeCommandApplication extends CommandApplication<ReportDivertTimeCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane pilot, ReportDivertTimeCommand c) {
    pilot.getWriter().reportDivertTimeLeft();
    ApplicationResult ret = ApplicationResult.getEmpty();
    return ret;
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane pilot, ReportDivertTimeCommand c) {
    PlaneRejection ret;

    if (pilot.getReader().isDeparture())
      ret = new PlaneRejection(c, "We do not have divert time as we are a departure.");
    else if (pilot.getReader().getState().is(AirplaneState.longFinal, AirplaneState.shortFinal)) {
      ret = new PlaneRejection(c, "We cannot report divert time at this moment.");
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
