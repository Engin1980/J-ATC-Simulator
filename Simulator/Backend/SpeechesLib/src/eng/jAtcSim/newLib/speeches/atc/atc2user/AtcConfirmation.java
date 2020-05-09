package eng.jAtcSim.newLib.speeches.atc.atc2user;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.speeches.atc.IAtcSpeech;
import eng.jAtcSim.newLib.speeches.base.Confirmation;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class AtcConfirmation extends Confirmation<IAtcSpeech> implements IAtcSpeech {
  public AtcConfirmation(IAtcSpeech origin) {
    super(origin);
  }
}
