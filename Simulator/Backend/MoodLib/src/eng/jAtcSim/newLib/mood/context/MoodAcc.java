package eng.jAtcSim.newLib.mood.context;

import eng.jAtcSim.newLib.mood.MoodManager;

public class MoodAcc implements IMoodAcc {

  private final MoodManager moodManager;

  public MoodAcc(MoodManager moodManager) {
    this.moodManager = moodManager;
  }

  @Override
  public MoodManager getMoodManager() {
    return moodManager;
  }
}
