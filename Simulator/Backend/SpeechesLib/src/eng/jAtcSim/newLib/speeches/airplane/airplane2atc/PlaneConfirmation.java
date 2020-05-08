package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.Confirmation;

public class PlaneConfirmation extends Confirmation<ICommand> implements IPlaneSpeech {
  public PlaneConfirmation(ICommand origin) {
    super(origin);
  }
}
