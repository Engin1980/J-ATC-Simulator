package eng.jAtcSim.newLib.speaking.fromAirplane.notifications.commandResponses;

import eng.jAtcSim.newLib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.newLib.speaking.fromAirplane.IAirplaneNotification;

public abstract class CommandResponse implements IAirplaneNotification {

  private final IAtcCommand origin;

  public CommandResponse(IAtcCommand origin) {
    if (origin == null) {
      throw new IllegalArgumentException("Value of {origin} cannot not be null.");
    }

    this.origin = origin;
  }

  protected CommandResponse(){
    origin = null;
  }

  public IAtcCommand getOrigin() {
    return origin;
  }

}
