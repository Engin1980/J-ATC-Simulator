package eng.jAtcSim.newLib.speaking.fromAirplane.notifications.commandResponses;

import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;

public class Confirmation extends CommandResponse {

  private Confirmation (){super();}

  public Confirmation(IAtcCommand origin) {
    super(origin);
  }

  @Override
  public String toString(){
    String ret = "Confirmation of |:" + super.getOrigin().toString() + ":| {notification}";

    return ret;
  }
}
