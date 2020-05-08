package eng.jAtcSim.newLib.speeches.atc;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.base.Rejection;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AtcRejection extends Rejection<IAtcSpeech> implements IAtcSpeech {
  public AtcRejection(IAtcSpeech origin, String reason) {
    super(origin, reason);
  }
}
