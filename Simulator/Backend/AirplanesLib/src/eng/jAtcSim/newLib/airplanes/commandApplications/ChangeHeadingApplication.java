package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.LocalInstanceProvider;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.Navigator;
import eng.jAtcSim.newLib.shared.enums.LeftRight;
import eng.jAtcSim.newLib.shared.enums.LeftRightAny;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;

public class ChangeHeadingApplication extends CommandApplication<ChangeHeadingCommand> {

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand pilot, ChangeHeadingCommand c) {
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
  protected ApplicationResult adjustAirplane(IAirplaneCommand pilot, ChangeHeadingCommand c) {
    if (pilot.getState() == Airplane.State.holding)
      pilot.abortHolding();

    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = pilot.getHeading();
    } else {
      targetHeading =
          Headings.add(
              c.getHeading(),
              LocalInstanceProvider.getAirport().getDeclination());
    }
    LeftRight turn;

    if (c.getDirection() == LeftRightAny.any) {
      turn =
          Navigator.getBetterDirectionToTurn(pilot.getHeading(), c.getHeading());
    } else {
      turn = c.getDirection().toLeftRight();
    }


    pilot.setTargetHeading(targetHeading, turn);

    return ApplicationResult.getEmpty();
  }
}