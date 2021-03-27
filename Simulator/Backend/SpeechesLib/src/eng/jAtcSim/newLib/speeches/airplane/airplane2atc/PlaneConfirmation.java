package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.Confirmation;
import exml.annotations.XConstructor;

public class PlaneConfirmation extends Confirmation<ICommand> implements IFromPlaneSpeech {

  @XConstructor
  public PlaneConfirmation(ICommand origin) {
    super(origin);
  }

  @Override
  public String toString() {
    return "Confirmation of '" + getOrigin().toString() + "' {PlaneConfirmation}";
  }
}
