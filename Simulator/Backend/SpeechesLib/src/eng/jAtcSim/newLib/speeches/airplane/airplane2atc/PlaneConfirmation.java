package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.Confirmation;

public class PlaneConfirmation extends Confirmation<ICommand> implements IFromPlaneSpeech {
  public PlaneConfirmation(ICommand origin) {
    super(origin);
  }
}
