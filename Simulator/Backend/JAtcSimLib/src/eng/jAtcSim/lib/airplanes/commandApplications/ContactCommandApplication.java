package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneWriteSimple;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ToDoException;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneWriteSimple plane, ContactCommand c) {
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
    plane.tuneAtc(a);
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    IFromAirplane s = new GoodDayNotification(
        plane.getFlightModule().getCallsign(),
        plane.getSha().getAltitude(),
        plane.getSha().getTargetAltitude(),
        plane.getEmergencyModule().isEmergency(),
        false);
    plane.sendMessage(s);
    //TODO when everything is done, I should update this to report ga-reason to the newly switched atc
    throw new ToDoException();
//    if (plane.getFlightModule().isArrival())
//      plane.adviceGoAroundReasonToAtcIfAny();
    // return ApplicationResult.getEmpty();
  }

  @Override
  protected IFromAirplane checkCommandSanity(IAirplaneWriteSimple plane, ContactCommand c) {
    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[0];
  }
}
