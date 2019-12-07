package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.speaking.IFromAirplane;
import eng.jAtcSim.newLib.speaking.fromAirplane.notifications.GoingAroundNotification;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.GoAroundCommand;

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
