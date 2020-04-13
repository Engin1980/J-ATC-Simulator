package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LAcc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedForTakeoffCommand;

public class ClearedForTakeOffCommandApplication extends CommandApplication<ClearedForTakeoffCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow,
        Airplane.State.arrivingCloseFaf,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.holding
    };
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ClearedForTakeoffCommand c) {
    Rejection ret = null;
    if (LAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName()) == null){
      ret =  new Rejection(c, "Unable to find runway threshold " + c.getRunwayThresholdName());
    }
    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ClearedForTakeoffCommand c) {
    ActiveRunwayThreshold threshold = LAcc.getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    plane.takeOff(threshold);
    return ApplicationResult.getEmpty();
  }
}
