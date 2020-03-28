package eng.jAtcSim.newLib.speeches.airplane2atc;

import eng.jAtcSim.newLib.speeches.INotification;

public class DivertTimeNotification implements INotification {
  private int minutesToDivert;

  public DivertTimeNotification(int minutesToDivert) {
    this.minutesToDivert = minutesToDivert;
  }

  public int getMinutesToDivert() {
    return minutesToDivert;
  }
}
