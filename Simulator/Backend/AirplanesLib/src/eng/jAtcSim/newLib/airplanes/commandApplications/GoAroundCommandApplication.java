package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.GoAroundCommand;

public class GoAroundCommandApplication extends CommandApplication<GoAroundCommand>{

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround,
        AirplaneState.departingLow,
        AirplaneState.departingHigh,
        AirplaneState.arrivingHigh,
        AirplaneState.arrivingLow,
        AirplaneState.arrivingCloseFaf,
        AirplaneState.landed,
        AirplaneState.holding
    };
  }

  @Override
  protected Rejection checkCommandSanity(Airplane plane, GoAroundCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, GoAroundCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getWriter().goAround(GoingAroundNotification.GoAroundReason.atcDecision);

    return ret;
  }
}
