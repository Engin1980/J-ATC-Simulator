package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.Confirmation;
import eng.newXmlUtils.annotations.XmlConstructor;
import eng.newXmlUtils.annotations.XmlConstructorParameter;
import exml.annotations.XConstructor;

public class PlaneConfirmation extends Confirmation<ICommand> implements IFromPlaneSpeech {

  @XConstructor
  @XmlConstructor
  public PlaneConfirmation(ICommand origin) {
    super(origin);
  }

  @Override
  public String toString() {
    return "Confirmation of '" + getOrigin().toString() + "' {PlaneConfirmation}";
  }
}
