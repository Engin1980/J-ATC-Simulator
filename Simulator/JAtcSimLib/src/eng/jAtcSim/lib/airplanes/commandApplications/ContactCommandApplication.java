package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected ApplicationResult adjustAirplane(IPilotWriteSimple pilot, ContactCommand c) {
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
    pilot.tuneAtc(a);
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    IFromAirplane s = new GoodDayNotification(
        pilot.getPlane().getFlight().getCallsign(),
        pilot.getPlane().getSha().getAltitude(),
        pilot.getPlane().getSha().getTargetAltitude(),
        pilot.getPlane().getEmergencyModule().isEmergency(),
        false);
    pilot.passMessageToAtc(s);
    //TODO when everything is done, I should update this to report ga-reason to the newly switched atc
    throw new ToDoException();
//    if (pilot.getPlane().getFlight().isArrival())
//      pilot.adviceGoAroundReasonToAtcIfAny();
    // return ApplicationResult.getEmpty();
  }

  @Override
  protected IFromAirplane checkCommandSanity(IPilotWriteSimple pilot, ContactCommand c) {

    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[0];
  }
}
