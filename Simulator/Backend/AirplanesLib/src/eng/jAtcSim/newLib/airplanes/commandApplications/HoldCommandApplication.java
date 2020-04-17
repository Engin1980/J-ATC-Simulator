package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.airplanes.accessors.IPlaneInterface;
import eng.jAtcSim.newLib.area.Navaid;
import eng.jAtcSim.newLib.area.PublishedHold;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.HoldCommand;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class HoldCommandApplication extends CommandApplication<HoldCommand> {

  @Override
  protected ApplicationResult adjustAirplane(IPlaneInterface plane, HoldCommand c) {
    if (c.isPublished()) {
      PublishedHold h = Gimme.tryGetPublishedHold(c.getNavaidName());
      plane.hold(h.getNavaid(), h.getInboundRadial(), h.getTurn());
    } else {
      Navaid n = Gimme.tryGetNavaid(c.getNavaidName());
      plane.hold(n, c.getInboundRadial(), c.getTurn());
    }
    return ApplicationResult.getEmpty();
  }

  @Override
  protected Rejection checkCommandSanity(IPlaneInterface plane, HoldCommand c) {
    if (Gimme.tryGetNavaid(c.getNavaidName()) == null)
      return super.getIllegalNavaidRejection(c.getNavaidName());

    if (c.isPublished() && Gimme.tryGetPublishedHold(c.getNavaidName()) == null)
        return new Rejection(c, sf("Unable to find a published hold from fix '%s'.", c.getNavaidName()));

    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{
        Airplane.State.holdingPoint,
        Airplane.State.takeOffRoll,
        Airplane.State.takeOffGoAround,
        Airplane.State.flyingIaf2Faf,
        Airplane.State.approachEnter,
        Airplane.State.approachDescend,
        Airplane.State.longFinal,
        Airplane.State.shortFinal,
        Airplane.State.landed
    };
  }
}
