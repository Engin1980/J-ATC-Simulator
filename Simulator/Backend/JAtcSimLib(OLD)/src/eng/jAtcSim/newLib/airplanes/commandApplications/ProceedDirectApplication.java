package eng.jAtcSim.newLib.area.airplanes.commandApplications;

import eng.jAtcSim.newLib.area.airplanes.Airplane;
import eng.jAtcSim.newLib.area.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.area.airplanes.navigators.ToCoordinateNavigator;
import eng.jAtcSim.newLib.area.speaking.IFromAirplane;
import eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.newLib.area.speaking.fromAtc.commands.ProceedDirectCommand;

public class ProceedDirectApplication extends CommandApplication<ProceedDirectCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, ProceedDirectCommand c) {
    IFromAirplane ret = null;

    if (c.getNavaid() == null)
      ret = new Rejection("Unknown point.", c);

    return ret;

  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, ProceedDirectCommand c) {
    if (plane.getState() == Airplane.State.holding) {
      plane.getAdvanced().abortHolding();
    }

    plane.setNavigator(
        new ToCoordinateNavigator(c.getNavaid().getCoordinate()));
    return ApplicationResult.getEmpty();
  }
}
