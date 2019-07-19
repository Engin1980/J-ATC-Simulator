package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.GoAroundCommand;

public class GoAroundCommandApplication extends CommandApplication<GoAroundCommand>{

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow,
        Airplane.State.arrivingCloseFaf,
        Airplane.State.landed,
        Airplane.State.holding
    };
  }

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, GoAroundCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, GoAroundCommand c) {
    ApplicationResult ret = new ApplicationResult();

    plane.getAdvanced().goAround(GoingAroundNotification.GoAroundReason.atcDecision);

    return ret;
  }
}
