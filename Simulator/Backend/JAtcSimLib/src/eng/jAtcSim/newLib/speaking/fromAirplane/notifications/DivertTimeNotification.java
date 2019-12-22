package eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.area.speaking.fromAirplane.IAirplaneNotification;

public class DivertTimeNotification implements IAirplaneNotification {
  private int minutesToDivert;

  public DivertTimeNotification(int minutesToDivert) {
    this.minutesToDivert = minutesToDivert;
  }

  public int getMinutesToDivert() {
    return minutesToDivert;
  }
}
