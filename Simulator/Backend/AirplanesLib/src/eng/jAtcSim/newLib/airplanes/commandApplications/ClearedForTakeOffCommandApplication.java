package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.contextLocal.Context;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ClearedForTakeoffCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ClearedForTakeOffCommandApplication extends CommandApplication<ClearedForTakeoffCommand> {

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
            AirplaneState.takeOffRoll,
            AirplaneState.takeOffGoAround,
            AirplaneState.departingLow,
            AirplaneState.departingHigh,
            AirplaneState.arrivingHigh,
            AirplaneState.arrivingLow,
            AirplaneState.arrivingCloseFaf,
            AirplaneState.flyingIaf2Faf,
            AirplaneState.approachEnter,
            AirplaneState.approachDescend,
            AirplaneState.longFinal,
            AirplaneState.shortFinal,
            AirplaneState.landed,
            AirplaneState.holding
    };
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ClearedForTakeoffCommand c) {
    PlaneRejection ret = null;
    ActiveRunwayThreshold threshold = Context.getArea().getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    if (threshold == null) {
      ret = new PlaneRejection(c, "Unable to find runway threshold " + c.getRunwayThresholdName());
    } else if (threshold != plane.getReader().getRouting().getAssignedRunwayThreshold())
      ret = new PlaneRejection(c, sf("We are cleared for takeoff via %s, but previously assigned was %s.",
              c.getRunwayThresholdName(), plane.getReader().getRouting().getAssignedRunwayThreshold().getName()));

    return ret;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ClearedForTakeoffCommand c) {
    ActiveRunwayThreshold threshold = Context.getArea().getAirport().tryGetRunwayThreshold(c.getRunwayThresholdName());
    EAssert.isTrue(threshold == plane.getReader().getRouting().getAssignedRunwayThreshold());
    plane.getWriter().startTakeOff();
    return ApplicationResult.getEmpty();
  }
}
