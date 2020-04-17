package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedToRouteCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedToRouteApplication extends CommandApplication<ClearedToRouteCommand> {

  @Override
  protected Rejection checkCommandSanity(IPlaneInterface plane, ClearedToRouteCommand c) {

    DARoute route = Gimme.tryGetDARoute(c.getRouteName());
    if (route == null)
      return new Rejection(sf("Unable to find route '%s'.", c.getRouteName()), c);

    if (plane.isArrival() && route.getType() == DARouteType.sid)
      return new Rejection("We are arrival, cannot be cleared to SID route.", c);
    else if (plane.isDeparture() && route.getType() != DARouteType.sid)
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
  protected ApplicationResult adjustAirplane(IPlaneInterface plane, ClearedToRouteCommand c) {
    ActiveRunwayThreshold threshold = Gimme.tryGetRunwayThreshold(c.getExpectedRunwayThresholdName());
    DARoute route = Gimme.tryGetDARoute(c.getRouteName());
    plane.setRouting(route, threshold);
    return ApplicationResult.getEmpty();
  }
}
