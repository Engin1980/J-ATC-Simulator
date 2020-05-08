package eng.jAtcSim.newLib.speeches.airplane.airplane2atc.responses;

import eng.jAtcSim.newLib.speeches.airplane.airplane2atc.PlaneRejection;
import eng.jAtcSim.newLib.speeches.airplane.atc2airplane.ThenCommand;

public class IllegalThenCommandRejection extends PlaneRejection {

  public IllegalThenCommandRejection(ThenCommand origin, String reason) {
    super(origin,reason);
  }

  @Override
  public String toString(){
    String ret = "{then} messageType due to " + super.getReason();
    return ret;
  }
}
