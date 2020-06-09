package eng.jAtcSim.newLib.mood.context;

import eng.eSystem.functionalInterfaces.Producer;
import eng.jAtcSim.newLib.mood.MoodManager;

public class MoodAcc {
  private static Producer<MoodManager> moodManagerProducer;

  public static MoodManager getMoodManager() {
    return moodManagerProducer.produce();
  }

  public static void setMoodManagerProducer(Producer<MoodManager> moodManagerProducer) {
    MoodAcc.moodManagerProducer = moodManagerProducer;
  }
}
