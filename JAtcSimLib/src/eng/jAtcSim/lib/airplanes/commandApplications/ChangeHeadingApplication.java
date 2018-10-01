package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.eSystem.geo.Coordinate;
import eng.jAtcSim.lib.global.Headings;
import eng.jAtcSim.lib.global.HeadingsNew;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class ChangeHeadingApplication extends CommandApplication<ChangeHeadingCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(Airplane.Airplane4Command plane, ChangeHeadingCommand c) {
    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ChangeHeadingCommand c) {
    if (plane.getState() == Airplane.State.holding)
      plane.getPilot().abortHolding();

    plane.getPilot().setTargetCoordinate((Coordinate)null);

    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = plane.getHeading();
    } else {
      targetHeading =
          Headings.add(
              c.getHeading(),
              Acc.airport().getDeclination());
    }
    boolean leftTurn;

    if (c.getDirection() == ChangeHeadingCommand.eDirection.any) {
      leftTurn
          = (HeadingsNew.getBetterDirectionToTurn(plane.getHeading(), c.getHeading()) == ChangeHeadingCommand.eDirection.left);
    } else {
      leftTurn
          = c.getDirection() == ChangeHeadingCommand.eDirection.left;
    }


    plane.getPilot().setTargetHeading(targetHeading, leftTurn);

    return ApplicationResult.getEmpty();
  }
}
