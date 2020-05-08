package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.INotification;

public class DivertTimeNotification implements INotification {
  private final int minutesToDivert;

  public DivertTimeNotification(int minutesToDivert) {
    this.minutesToDivert = minutesToDivert;
  }

  public int getMinutesToDivert() {
    return minutesToDivert;
  }
}
