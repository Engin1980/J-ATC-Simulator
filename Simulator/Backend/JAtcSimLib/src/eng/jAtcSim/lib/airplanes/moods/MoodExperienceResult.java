package eng.jAtcSim.lib.airplanes.moods;

import eng.jAtcSim.lib.global.ETime;

public class MoodExperienceResult {
  private ETime time;
  private String description;
  private int points;

   MoodExperienceResult(ETime time, String description, int points) {
    this.time = time;
    this.description = description;
    this.points = points;
  }

  public ETime getTime() {
    return time;
  }

  public String getDescription() {
    return description;
  }

  public int getPoints() {
    return points;
  }
}
