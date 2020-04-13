package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.HoldCommand;

public class HoldCommandApplication extends CommandApplication<HoldCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, HoldCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, HoldCommand c) {
    plane.hold(c.getNavaidName(), c.getInboundRadial(), c.getTurn());
    return ApplicationResult.getEmpty();
  }
}
