package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.context.AreaAcc;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedForTakeoffCommand;

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
  protected PlaneRejection checkCommandSanity(Airplane plane, ClearedForTakeoffCommand c) {
    PlaneRejection ret = null;
    if (AreaAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName()) == null){
      ret =  new PlaneRejection(c, "Unable to find runway threshold " + c.getRunwayThresholdName());
    }
    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedForTakeoffCommand c) {
    ActiveRunwayThreshold threshold = AreaAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    plane.getWriter().startTakeOff(threshold);
    return ApplicationResult.getEmpty();
  }
}
