package eng.jAtcSim.newLib.mood;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

public class MoodResult {
  private EDayTimeStamp time;
  private Callsign callsing;
  private IList<MoodExperienceResult> experiences;

  @XmlConstructor private MoodResult(){
    PostContracts.register(this, () -> time != null && callsing != null);
  }

  public MoodResult(EDayTimeStamp time, Callsign callsign, IList<MoodExperienceResult> experiences) {
    this.time = time;
    this.callsing = callsign;
    this.experiences = experiences;
  }

  public Callsign getCallsing() {
    return callsing;
  }

  public IReadOnlyList<MoodExperienceResult> getExperiences() {
    return experiences;
  }

  public int getPoints() {
    int ret = experiences.sumInt(q -> q.getPoints());
    return ret;
  }

  public EDayTimeStamp getTime() {
    return time;
  }
}
