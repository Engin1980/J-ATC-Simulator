package eng.jAtcSim.newLib.mood.context;

import eng.jAtcSim.newLib.mood.MoodManager;

public class MoodContext implements IMoodContext {

  private final MoodManager moodManager;

  public MoodContext(MoodManager moodManager) {
    this.moodManager = moodManager;
  }

  @Override
  public MoodManager getMoodManager() {
    return moodManager;
  }
}
