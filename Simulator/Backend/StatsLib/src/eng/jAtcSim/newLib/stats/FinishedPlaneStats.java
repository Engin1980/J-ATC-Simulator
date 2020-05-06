package eng.jAtcSim.newLib.stats;

import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.Callsign;

public class FinishedPlaneStats {
  private final int delayDifference;
  private final Callsign callsign;
  private final MoodResult moodResult;
  private final boolean departure;
  private final boolean emergency;

  public FinishedPlaneStats(Callsign callsign, boolean departure, boolean emergency, int delayDifference, MoodResult moodResult) {
    this.delayDifference = delayDifference;
    this.callsign = callsign;
    this.moodResult = moodResult;
    this.departure = departure;
    this.emergency = emergency;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getDelayDifference() {
    return delayDifference;
  }

  public MoodResult getMoodResult() {
    return moodResult;
  }

  public boolean isArrival() {
    return !departure;
  }

  public boolean isDeparture() {
    return departure;
  }

  public boolean isEmergency() {
    return emergency;
  }
}
