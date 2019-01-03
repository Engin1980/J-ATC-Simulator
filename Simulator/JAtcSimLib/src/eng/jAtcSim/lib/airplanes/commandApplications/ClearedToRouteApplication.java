package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses.Rejection;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedToRouteCommand;
import eng.jAtcSim.lib.world.Route;

public class ClearedToRouteApplication extends CommandApplication<ClearedToRouteCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ClearedToRouteCommand c) {
    if (plane.isArrival() && c.getRoute().getType() == Route.eType.sid)
      return new Rejection("We are arrival, cannot be cleared to SID route.", c);
    else if (!plane.isArrival() && c.getRoute().getType() != Route.eType.sid)
      return new Rejection("We are departure, can be cleared only to SID route",c );

    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[] {
        Airplane.State.arrivingCloseFaf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ClearedToRouteCommand c) {
    plane.getPilot().setRoute(c.getRoute(), c.getExpectedRunwayThreshold());
    return ApplicationResult.getEmpty();
  }
}
