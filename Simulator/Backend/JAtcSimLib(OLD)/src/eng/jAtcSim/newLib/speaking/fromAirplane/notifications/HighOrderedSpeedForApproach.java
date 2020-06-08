package eng.jAtcSim.newLib.area.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.area.speaking.fromAirplane.IAirplaneNotification;

public class HighOrderedSpeedForApproach implements IAirplaneNotification {
  private int orderedSpeed;
  private int requiredSpeed;

  public int getOrderedSpeed() {
    return orderedSpeed;
  }

  public int getRequiredSpeed() {
    return requiredSpeed;
  }

  public HighOrderedSpeedForApproach(int orderedSpeed, int requiredSpeed) {
    this.orderedSpeed = orderedSpeed;
    this.requiredSpeed = requiredSpeed;
  }
}
