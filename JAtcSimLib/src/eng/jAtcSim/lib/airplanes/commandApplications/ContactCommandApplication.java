package eng.jAtcSim.lib.airplanes.commandApplications;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.Airplane;
import eng.jAtcSim.lib.atcs.Atc;
import eng.jAtcSim.lib.exceptions.ENotSupportedException;
import eng.jAtcSim.lib.speaking.IFromAirplane;
import eng.jAtcSim.lib.speaking.ISpeech;
import eng.jAtcSim.lib.speaking.fromAirplane.notifications.GoodDayNotification;
import eng.jAtcSim.lib.speaking.fromAtc.commands.ContactCommand;

public class ContactCommandApplication extends CommandApplication<ContactCommand> {

  @Override
  protected IFromAirplane checkSanity(Airplane.Airplane4Command plane, ContactCommand c) {
    // I hope confirmation is done automatically
//    Atc a;
//    switch (c.getAtcType()) {
//      case app:
//        a = Acc.atcApp();
//        break;
//      case ctr:
//        a = Acc.atcCtr();
//        break;
//      case twr:
//        a = Acc.atcTwr();
//        break;
//      default:
//        throw new ENotSupportedException();
//    }
//    // confirmation to previous atc
//    confirmIfReq(c);
//    flushSaidTextToAtc();

    return null;
  }

  @Override
  protected ApplicationResult adjustAirplane(Airplane.Airplane4Command plane, ContactCommand c) {
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
        throw new ENotSupportedException();
    }
    // confirmation to previous atc


    // contacting next atc
    plane.getPilot().setResponsibleAtc(a);
    // rewritten
    // TODO now switch is realised in no-time, there is no delay between "frequency change confirmation" and "new atc call"
    ISpeech s = new GoodDayNotification(plane.getCallsign(), Acc.toAltS(plane.getAltitude(), true));
    plane.getPilot().say(s);
    return ApplicationResult.getEmpty();
  }
}
