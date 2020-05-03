package eng.jAtcSim.newLib.airplanes.commandApplications;


import eng.eSystem.geo.Headings;
import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.airplanes.modules.sha.navigators.HeadingNavigator;
import eng.jAtcSim.newLib.area.AreaAcc;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ChangeHeadingCommand;

public class ChangeHeadingApplication extends CommandApplication<ChangeHeadingCommand> {

  @Override
  protected Rejection checkCommandSanity(Airplane pilot, ChangeHeadingCommand c) {
    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane pilot, ChangeHeadingCommand c) {
    if (pilot.getReader().getState() == AirplaneState.holding)
      pilot.getWriter().abortHolding();

    double targetHeading;
    if (c.isCurrentHeading()) {
      targetHeading = pilot.getReader().getSha().getHeading();
    } else {
      targetHeading =
          Headings.add(
              c.getHeading(),
              AreaAcc.getAirport().getDeclination());
    }

    pilot.getWriter().setTargetHeading(new HeadingNavigator(targetHeading, c.getDirection()));

    return ApplicationResult.getEmpty();
  }
}
