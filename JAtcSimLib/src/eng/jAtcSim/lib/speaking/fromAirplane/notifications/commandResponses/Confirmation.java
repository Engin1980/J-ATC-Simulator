package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.speaking.ICommand;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;

public class Confirmation extends CommandResponse {

  public Confirmation(IAtcCommand origin) {
    super(origin);
  }

  @Override
  public String toString(){
    String ret = "Confirmation of |:" + super.getOrigin().toString() + ":| {notification}";

    return ret;
  }
}
