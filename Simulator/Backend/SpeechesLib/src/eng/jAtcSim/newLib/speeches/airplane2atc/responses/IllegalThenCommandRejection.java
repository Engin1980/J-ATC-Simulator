package eng.jAtcSim.newLib.speeches.airplane2atc.responses;

import eng.jAtcSim.newLib.speeches.Rejection;
import eng.jAtcSim.newLib.speeches.atc2airplane.ThenCommand;

public class IllegalThenCommandRejection extends Rejection {

  public IllegalThenCommandRejection(ThenCommand origin, String reason) {
    super(origin,reason);
  }

  @Override
  public String toString(){
    String ret = "{then} messageType due to " + super.getReason();
    return ret;
  }
}
