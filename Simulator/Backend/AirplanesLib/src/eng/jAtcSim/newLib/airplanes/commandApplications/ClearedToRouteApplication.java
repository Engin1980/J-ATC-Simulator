package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.internal.InternalAcc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToRouteCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedToRouteApplication extends CommandApplication<ClearedToRouteCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedToRouteCommand c) {
    ActiveRunwayThreshold threshold = InternalAcc.tryGetRunwayThreshold(c.getExpectedRunwayThresholdName());
    DARoute route = InternalAcc.tryGetDARoute(c.getRouteName());
    plane.getWriter().setDaRouting(route, threshold);
    return ApplicationResult.getEmpty();
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ClearedToRouteCommand c) {

    DARoute route = InternalAcc.tryGetDARoute(c.getRouteName());
    if (route == null)
      return new PlaneRejection(c, sf("Unable to find route '%s'.", c.getRouteName()));

    if (plane.getReader().isArrival() && route.getType() == DARouteType.sid)
      return new PlaneRejection(c, "We are arrival, cannot be cleared to SID route.");
    else if (plane.getReader().isDeparture() && route.getType() != DARouteType.sid)
      return new PlaneRejection(c, "We are departure, can be cleared only to SID route");

    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.arrivingCloseFaf,
        AirplaneState.approachEntry,
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
}
