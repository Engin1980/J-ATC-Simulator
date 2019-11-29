package eng.jAtcSim.lib.airplanes.moods;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;
import sun.security.krb5.internal.ETypeInfo;

public class MoodResult {
  private ETime time;
  private Callsign callsing;
  private IList<MoodExperienceResult> experiences;

  public MoodResult(ETime time, Callsign callsign, IList<MoodExperienceResult> experiences) {
    this.time = time;
    this.callsing = callsign;
    this.experiences = experiences;
  }

  public IReadOnlyList<MoodExperienceResult> getExperiences() {
    return experiences;
  }

  public int getPoints() {
    int ret = experiences.sumInt(q -> q.getPoints());
    return ret;
  }

  public ETime getTime() {
    return time;
  }

  public Callsign getCallsing() {
    return callsing;
  }
}
