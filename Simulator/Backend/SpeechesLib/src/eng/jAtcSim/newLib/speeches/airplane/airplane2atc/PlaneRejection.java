package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;
import eng.jAtcSim.newLib.speeches.airplane.IPlaneSpeech;
import eng.jAtcSim.newLib.speeches.base.Rejection;

public class PlaneRejection extends Rejection<ICommand> implements IPlaneSpeech {

  public PlaneRejection(ICommand origin, String reason) {
    super(origin, reason);
    EAssert.Argument.isNonemptyString(reason);
  }

}
