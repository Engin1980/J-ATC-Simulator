package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class ChangeHeadingApplication extends CommandApplication<ChangeHeadingCommand> {
  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ChangeHeadingCommand c) {
    IFromAirplane ret;
    ret = super.checkInvalidState(plane,c,
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed);
      if (ret != null) return ret;

    return null;

  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ChangeHeadingCommand c) {
    if (plane.getState() == Airplane.State.holding)
      plane.getPilot().abortHolding();

    plane.getPilot().setTargetCoordinate(null);

    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = plane.getHeading();
    } else {
      targetHeading = c.getHeading();
    }
    boolean leftTurn;

    if (c.getDirection() == ChangeHeadingCommand.eDirection.any) {
      leftTurn
          = (Headings.getBetterDirectionToTurn(plane.getHeading(), c.getHeading()) == ChangeHeadingCommand.eDirection.left);
    } else {
      leftTurn
          = c.getDirection() == ChangeHeadingCommand.eDirection.left;
    }

    plane.getPilot().setTargetHeading((int) targetHeading, leftTurn);

    return ApplicationResult.getEmpty();
  }
}
