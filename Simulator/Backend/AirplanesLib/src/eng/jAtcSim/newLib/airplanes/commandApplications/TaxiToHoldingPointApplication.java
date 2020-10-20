package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.TaxiToHoldingPointCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class TaxiToHoldingPointApplication extends CommandApplication<TaxiToHoldingPointCommand> {
  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, TaxiToHoldingPointCommand c) {
    if (tryGetThreshold(c.getRunwayThresholdName()) == null) {
      return new PlaneRejection(c, sf("There is no threshold named '%s' at this airport.", c.getRunwayThresholdName()));
    }
    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return AirplaneState.valuesExcept(AirplaneState.holdingPoint);
  }

  private ActiveRunwayThreshold tryGetThreshold(String name) {
    ActiveRunwayThreshold ret;
    if (Context.getArea().getAirport().getAllThresholds().isNone(
            q -> q.getName().equals(name)))
      ret = null;
    else
      ret = Context.getArea().getAirport().getRunwayThreshold(name);
    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, TaxiToHoldingPointCommand c) {
    ActiveRunwayThreshold t = tryGetThreshold(c.getRunwayThresholdName());
    plane.getWriter().setHoldingPoint(t);
    return ApplicationResult.getEmpty();
  }
}
