package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ClearedForTakeoffCommand;

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
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, ClearedForTakeoffCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, ClearedForTakeoffCommand c) {
    plane.getAdvanced().takeOff(c.getRunwayThreshold());
    return ApplicationResult.getEmpty();
  }
}
