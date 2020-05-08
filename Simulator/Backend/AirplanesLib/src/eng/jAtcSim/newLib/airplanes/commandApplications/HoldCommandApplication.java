package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.HoldCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class HoldCommandApplication extends CommandApplication<HoldCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, HoldCommand c) {
    if (c.isPublished()) {
      PublishedHold h = Gimme.tryGetPublishedHold(c.getNavaidName());
      plane.getWriter().hold(h.getNavaid(), h.getInboundRadial(), h.getTurn());
    } else {
      Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
      plane.getWriter().hold(n, c.getInboundRadial(), c.getTurn());
    }
    return ApplicationResult.getEmpty();
  }

  @Override
  protected Rejection checkCommandSanity(Airplane plane, HoldCommand c) {
    if (Gimme.tryGetNavaid(c.getNavaidName()) == null)
      return super.getIllegalNavaidRejection(c.getNavaidName());

    if (c.isPublished() && Gimme.tryGetPublishedHold(c.getNavaidName()) == null)
        return new Rejection(c, sf("Unable to find a published hold from fix '%s'.", c.getNavaidName()));

    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{
        AirplaneState.holdingPoint,
        AirplaneState.takeOffRoll,
        AirplaneState.takeOffGoAround,
        AirplaneState.flyingIaf2Faf,
        AirplaneState.approachEnter,
        AirplaneState.approachDescend,
        AirplaneState.longFinal,
        AirplaneState.shortFinal,
        AirplaneState.landed
    };
  }
}
