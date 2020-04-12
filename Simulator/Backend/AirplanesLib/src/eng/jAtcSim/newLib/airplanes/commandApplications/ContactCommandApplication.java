package eng.jAtcSim.newLib.airplanes.commandApplications;

import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.airplanes.Airplane;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.airplane2atc.GoodDayNotification;
import eng.jAtcSim.newLib.speeches.atc2airplane.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected ApplicationResult adjustAirplane(IAirplaneCommand plane, ContactCommand c) {
    String a;
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
    GoodDayNotification s = new GoodDayNotification(
        plane.getCallsign(),
        plane.getAltitude(),
        plane.getTargetAltitude(),
        plane.isEmergency(),
        false);
    plane.sendMessage(s);
    //TODO when everything is done, I should update this to report ga-reason to the newly switched atc
    throw new ToDoException();
//    if (plane.getFlightModule().isArrival())
//      plane.adviceGoAroundReasonToAtcIfAny();
    // return ApplicationResult.getEmpty();
  }

  @Override
  protected Rejection checkCommandSanity(IAirplaneCommand plane, ContactCommand c) {
    return null;
  }

  @Override
  protected Airplane.State[] getInvalidStates() {
    return new Airplane.State[]{};
  }
}
