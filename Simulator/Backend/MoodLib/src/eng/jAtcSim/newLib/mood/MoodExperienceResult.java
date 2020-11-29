package eng.jAtcSim.newLib.mood;

import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

import java.util.Comparator;

public class MoodExperienceResult {

  public static class ByTimeComparer implements Comparator<MoodExperienceResult> {

    @Override
    public int compare(MoodExperienceResult o1, MoodExperienceResult o2) {
      EDayTimeStamp a = o1.time == null ? new EDayTimeStamp(0) : o1.time;
      EDayTimeStamp b = o2.time == null ? new EDayTimeStamp(0) : o2.time;
      return Integer.compare(a.getValue(), b.getValue());
    }
  }

  private EDayTimeStamp time;
  private String description;
  private int points;

  @XmlConstructor
  private MoodExperienceResult() {
    PostContracts.register(this, () -> time != null);
  }

  MoodExperienceResult(EDayTimeStamp time, String description, int points) {
    this.time = time;
    this.description = description;
    this.points = points;
  }

  public String getDescription() {
    return description;
  }

  public int getPoints() {
    return points;
  }

  public EDayTimeStamp getTime() {
    return time;
  }
}
