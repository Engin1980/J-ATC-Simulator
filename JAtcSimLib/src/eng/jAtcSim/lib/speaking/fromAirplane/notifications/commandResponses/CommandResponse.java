package eng.jAtcSim.lib.speaking.fromAirplane.notifications.commandResponses;

import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import jatcsimlib.speaking.ICommand;
import jatcsimlib.speaking.IFromAirplane;
import jatcsimlib.speaking.ISpeech;
import jatcsimlib.speaking.fromAirplane.IAirplaneNotification;
import jatcsimlib.speaking.fromAtc.IAtcCommand;

public abstract class CommandResponse implements IAirplaneNotification {

  private final IAtcCommand origin;

  public CommandResponse(IAtcCommand origin) {
    if (origin == null) {
      throw new IllegalArgumentException("Value of {origin} cannot not be null.");
    }

    this.origin = origin;
  }

  public IAtcCommand getOrigin() {
    return origin;
  }

}
