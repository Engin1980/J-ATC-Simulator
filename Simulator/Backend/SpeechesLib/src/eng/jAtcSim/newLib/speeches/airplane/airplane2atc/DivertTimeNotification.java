package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;

public class DivertTimeNotification implements IFromPlaneSpeech {
  private final int minutesToDivert;

  public DivertTimeNotification(int minutesToDivert) {
    this.minutesToDivert = minutesToDivert;
  }

  public int getMinutesToDivert() {
    return minutesToDivert;
  }
}
