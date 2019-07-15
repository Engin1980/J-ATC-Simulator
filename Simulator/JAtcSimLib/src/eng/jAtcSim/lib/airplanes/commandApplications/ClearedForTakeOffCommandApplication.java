package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;

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
  protected IFromAirplane checkCommandSanity(Pilot.Pilot5Command pilot, ClearedForTakeoffCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Pilot.Pilot5Command pilot, ClearedForTakeoffCommand c) {
    pilot.startTakeOff(c.getRunwayThreshold());
    return ApplicationResult.getEmpty();
  }
}
