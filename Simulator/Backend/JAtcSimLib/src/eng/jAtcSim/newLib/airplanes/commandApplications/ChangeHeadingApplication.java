package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.Acc;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.newLib.global.Headings;
import eng.jAtcSim.newLib.global.HeadingsNew;
import eng.jAtcSim.newLib.speaking.IFromAirplane;
import eng.jAtcSim.newLib.speaking.fromAtc.commands.ChangeHeadingCommand;

public class ChangeHeadingApplication extends CommandApplication<ChangeHeadingCommand> {

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple pilot, ChangeHeadingCommand c) {
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
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple pilot, ChangeHeadingCommand c) {
    if (pilot.getState() == Airplane.State.holding)
      pilot.getAdvanced().abortHolding();

    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = pilot.getSha().getHeading();
    } else {
      targetHeading =
          Headings.add(
              c.getHeading(),
              Acc.airport().getDeclination());
    }
    boolean leftTurn;

    if (c.getDirection() == ChangeHeadingCommand.eDirection.any) {
      leftTurn =
          HeadingsNew.getBetterDirectionToTurn(pilot.getSha().getHeading(), c.getHeading()) == ChangeHeadingCommand.eDirection.left;
    } else {
      leftTurn =
          c.getDirection() == ChangeHeadingCommand.eDirection.left;
    }


    pilot.setTargetHeading(targetHeading, leftTurn);

    return ApplicationResult.getEmpty();
  }
}
