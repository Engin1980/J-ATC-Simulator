package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.internal.InternalAcc;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.routes.DARoute;
import eng.jAtcSim.newLib.shared.enums.DARouteType;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedToRouteCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedToRouteApplication extends CommandApplication<ClearedToRouteCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedToRouteCommand c) {
    ActiveRunwayThreshold threshold = InternalAcc.tryGetRunwayThreshold(c.getExpectedRunwayThresholdName());
    DARoute route = isVectoringRoute(c.getRouteName())
            ? tryGetVectoringRoute(c.getRouteName())
            : InternalAcc.tryGetDARoute(c.getRouteName());
    EAssert.isNotNull(route);
    plane.getWriter().setDaRouting(route, threshold);
    return ApplicationResult.getEmpty();
  }

  private boolean isVectoringRoute(String routeName) {
    boolean ret = routeName.endsWith(DARoute.VECTORING_ROUTE_NAME_POSTFIX);
    return ret;
  }

  private DARoute tryGetVectoringRoute(String routeName) {
    DARoute ret;
    String navaidName = routeName.substring(0, routeName.length() - DARoute.VECTORING_ROUTE_NAME_POSTFIX.length());
    Navaid navaid = Context.getArea().getNavaids().tryGet(navaidName);
    if (navaid == null)
      ret = null;
    else
      ret = DARoute.createNewVectoringByFix(navaid);
    return ret;
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ClearedToRouteCommand c) {
    DARoute route;
    if (isVectoringRoute(c.getRouteName())) {
      route = tryGetVectoringRoute(c.getRouteName());
      if (route == null)
        return new PlaneRejection(c, sf("Unable to find vectoring route %s.", c.getRouteName()));
    } else {
      route = InternalAcc.tryGetDARoute(c.getRouteName());
      if (route == null)
        return new PlaneRejection(c, sf("Unable to find route '%s'.", c.getRouteName()));
    }

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
            AirplaneState.takeOff,
            AirplaneState.departingLow,
            AirplaneState.departingHigh,
            AirplaneState.landed
    };
  }
}
