package eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.area.speaking.fromAirplane.IAirplaneNotification;
import eng.jAtcSim.newLib.world.Navaid;

public class DivertingNotification implements IAirplaneNotification {
  private Navaid exitNavaid;

  public DivertingNotification(Navaid exitNavaid) {
    this.exitNavaid = exitNavaid;
  }

  public Navaid getExitNavaid() {
    return exitNavaid;
  }
}
