package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ClearedForTakeoffCommand;

public class ClearedForTakeOffCommandApplication extends CommandApplication<ClearedForTakeoffCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ClearedForTakeoffCommand c) {
    IFromAirplane ret;
    ret = super.checkInvalidState(plane, c,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.departingLow,
        Airplane.State.departingHigh,
        Airplane.State.arrivingHigh,
        Airplane.State.arrivingLow,
        Airplane.State.arrivingCloseFaf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed,
        Airplane.State.holding);
    if (ret != null) return ret;

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ClearedForTakeoffCommand c) {
    plane.setTakeOffPosition(c.getRunwayThreshold().getCoordinate());
    plane.getPilot().setTakeOffBehavior(c.getRunwayThreshold());
    return ApplicationResult.getEmpty();
  }
}
