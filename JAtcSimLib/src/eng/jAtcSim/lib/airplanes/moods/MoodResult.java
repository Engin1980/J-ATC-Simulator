package eng.jAtcSim.lib.airplanes.moods;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;

public class MoodResult {
  private IList<MoodExperienceResult> experiences;

  public MoodResult(IList<MoodExperienceResult> experiences) {
    this.experiences = experiences;
  }

  public IReadOnlyList<MoodExperienceResult> getExperiences() {
    return experiences;
  }

  public int getPoints() {
    int ret = experiences.sumInt(q -> q.getPoints());
    return ret;
  }
}
