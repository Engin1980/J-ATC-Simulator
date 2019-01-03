package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.lib.world.Navaid;

public class DivertingNotification implements IAirplaneNotification {
  private Navaid exitNavaid;

  public DivertingNotification(Navaid exitNavaid) {
    this.exitNavaid = exitNavaid;
  }

  public Navaid getExitNavaid() {
    return exitNavaid;
  }
}
