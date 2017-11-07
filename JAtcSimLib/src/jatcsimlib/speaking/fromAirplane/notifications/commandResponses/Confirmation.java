package jatcsimlib.speaking.fromAirplane.notifications.commandResponses;

import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.fromAtc.IAtcCommand;

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
