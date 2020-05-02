package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ClearedToRouteCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedToRouteApplication extends CommandApplication<ClearedToRouteCommand> {

  @Override
  protected Rejection checkCommandSanity(Airplane plane, ClearedToRouteCommand c) {

    DARoute route = Gimme.tryGetDARoute(c.getRouteName());
    if (route == null)
      return new Rejection(sf("Unable to find route '%s'.", c.getRouteName()), c);

    if (plane.getReader().isArrival() && route.getType() == DARouteType.sid)
      return new Rejection("We are arrival, cannot be cleared to SID route.", c);
    else if (plane.getReader().isDeparture() && route.getType() != DARouteType.sid)
      return new Rejection("We are departure, can be cleared only to SID route",c );

    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[] {
        AirplaneState.arrivingCloseFaf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround,
        AirplaneState.departingLow,
        AirplaneState.departingHigh,
        AirplaneState.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedToRouteCommand c) {
    ActiveRunwayThreshold threshold = Gimme.tryGetRunwayThreshold(c.getExpectedRunwayThresholdName());
    DARoute route = Gimme.tryGetDARoute(c.getRouteName());
    plane.getWriter().setRouting(route, threshold);
    return ApplicationResult.getEmpty();
  }
}
