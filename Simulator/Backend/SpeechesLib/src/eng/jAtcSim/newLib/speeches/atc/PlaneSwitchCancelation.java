package eng.jAtcSim.newLib.speeches.atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;

public class PlaneSwitchCancelation implements IAtcSpeech {

  public final Callsign callsign;

  public PlaneSwitchCancelation(Callsign callsign) {
    EAssert.Argument.isNotNull(callsign, "callsign");
    this.callsign = callsign;
  }

  public Callsign getCallsign() {
    return callsign;
  }
}
