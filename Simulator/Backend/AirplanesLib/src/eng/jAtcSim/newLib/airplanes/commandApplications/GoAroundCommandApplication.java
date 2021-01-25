package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
 import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.GoAroundCommand;

public class GoAroundCommandApplication extends CommandApplication<GoAroundCommand>{

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.takeOff,
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
  protected PlaneRejection checkCommandSanity(Airplane plane, GoAroundCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, GoAroundCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getWriter().goAround(GoingAroundNotification.GoAroundReason.orderedByAtc);

    return ret;
  }
}
