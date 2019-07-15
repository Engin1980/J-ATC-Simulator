package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.HoldCommand;

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
  protected IFromAirplane checkCommandSanity(Pilot.Pilot5Command pilot, HoldCommand c) {
    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Pilot.Pilot5Command pilot, HoldCommand c) {
    //TODO the first line is probably useless
    pilot.setTargetCoordinate((Coordinate) null);
    pilot.setHoldBehavior(c.getNavaid(), c.getInboundRadial(), c.isLeftTurn());
    return ApplicationResult.getEmpty();
  }
}
