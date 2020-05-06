package eng.jAtcSim.newLib.mood;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class MoodAcc {
  private static Producer<MoodManager> moodManagerProducer;

  public static MoodManager getMoodManager() {
    return moodManagerProducer.produce();
  }

  public static void setMoodManagerProducer(Producer<MoodManager> moodManagerProducer) {
    MoodAcc.moodManagerProducer = moodManagerProducer;
  }
}
