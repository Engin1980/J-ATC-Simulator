package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.jAtcSim.newLib.airplanes.AirplaneState;
import eng.jAtcSim.newLib.airplanes.internal.Airplane;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoingAroundNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected ApplicationResult adjustAirplane(Airplane plane, ContactCommand c) {
    // contacting next atc
    plane.getWriter().tuneAtc(c.getAtc());
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    GoodDayNotification s = new GoodDayNotification(
        plane.getReader().getCallsign(),
        plane.getReader().getSha().getAltitude(),
        plane.getReader().getSha().getTargetAltitude(),
        plane.getReader().isEmergency(),
        false);
    plane.getWriter().sendMessage(s);

    GoingAroundNotification.GoAroundReason lastGaReason = plane.getReader().getAndClearLastGoAroundReasonIfAny();
    if (lastGaReason != null)
    {
      GoingAroundNotification gan = new GoingAroundNotification(lastGaReason);
      plane.getWriter().sendMessage(gan);
    }

    return ApplicationResult.getEmpty();
  }

  @Override
  protected PlaneRejection checkCommandSanity(Airplane plane, ContactCommand c) {
    return null;
  }

  @Override
  protected AirplaneState[] getInvalidStates() {
    return new AirplaneState[]{};
  }
}
