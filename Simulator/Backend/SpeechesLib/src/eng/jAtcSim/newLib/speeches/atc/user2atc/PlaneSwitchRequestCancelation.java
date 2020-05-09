package eng.jAtcSim.newLib.speeches.atc.user2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.Squawk;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;

public class PlaneSwitchRequestCancelation implements IAtcSpeech {

  public final Squawk squawk;

  public PlaneSwitchRequestCancelation(Squawk squawk) {
    EAssert.Argument.isNotNull(squawk, "squawk");
    this.squawk = squawk;
  }

  public Squawk getSquawk() {
    return squawk;
  }
}
