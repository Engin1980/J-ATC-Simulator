package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.DivertCommand;

public class DivertCommandApplication extends CommandApplication<DivertCommand> {

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, DivertCommand c) {
    PlaneRejection ret = null;

    if (plane.getReader().isDeparture())
      ret = new PlaneRejection(c,"We are departing, we will not divert.");

    return ret;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEntry,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed,
        AirplaneState.takeOffGoAround
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, DivertCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getWriter().divert(true);

    return ret;
  }
}
