package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedForTakeoffCommand;

public class ClearedForTakeOffCommandApplication extends CommandApplication<ClearedForTakeoffCommand> {

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround,
        AirplaneState.departingLow,
        AirplaneState.departingHigh,
        AirplaneState.arrivingHigh,
        AirplaneState.arrivingLow,
        AirplaneState.arrivingCloseFaf,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed,
        AirplaneState.holding
    };
  }

  @Override
  protected Rejection checkCommandSanity(Airplane plane, ClearedForTakeoffCommand c) {
    Rejection ret = null;
    if (LAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName()) == null){
      ret =  new Rejection(c, "Unable to find runway threshold " + c.getRunwayThresholdName());
    }
    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedForTakeoffCommand c) {
    ActiveRunwayThreshold threshold = LAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    plane.getWriter().startTakeOff(threshold);
    return ApplicationResult.getEmpty();
  }
}
