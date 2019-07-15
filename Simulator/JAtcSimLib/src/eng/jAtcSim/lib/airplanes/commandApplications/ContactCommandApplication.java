package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.Pilot;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[0];
  }

  @Override
  protected IFromAirplane checkCommandSanity(Pilot.Pilot5Command pilot, ContactCommand c) {

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Pilot.Pilot5Command pilot, ContactCommand c) {
    Atc a;
    switch (c.getAtcType()) {
      case app:
        a = Acc.atcApp();
        break;
      case ctr:
        a = Acc.atcCtr();
        break;
      case twr:
        a = Acc.atcTwr();
        break;
      default:
        throw new EEnumValueUnsupportedException(c.getAtcType());
    }
    // confirmation to previous atc


    // contacting next atc
    pilot.setResponsibleAtc(a);
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    ISpeech s = new GoodDayNotification(pilot.getFlight().getCallsign(), pilot.getPlane().getAltitude(),
        pilot.getPlane().getTargetAltitude(), pilot.getPlane().isEmergency(), false);
    pilot.say(s);
    if (pilot.getFlight().isArrival()) pilot.adviceGoAroundReasonToAtcIfAny();
    return ApplicationResult.getEmpty();
  }
}
