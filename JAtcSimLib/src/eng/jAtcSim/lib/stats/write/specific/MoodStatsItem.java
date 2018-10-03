package eng.jAtcSim.lib.stats.write.specific;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.airplanes.moods.MoodExperienceResult;
import eng.jAtcSim.lib.airplanes.moods.MoodResult;
import eng.jAtcSim.lib.stats.read.shared.DataView;

public class MoodStatsItem  {
  private IList<MoodResult> moods = new EList<>();

  public void add(MoodResult evaluatedMood) {
    moods.add(evaluatedMood);
  }

  public DataView toView() {
    double min = Double.MAX_VALUE;
    double max = Double.MIN_VALUE;
    double sum = 0;

    for (MoodResult mood : moods) {
      min =Math.min(min, mood.getPoints());
      max =Math.max(max, mood.getPoints());
      sum += mood.getPoints();
    }
    double mean = moods.isEmpty() ? 0 : sum / moods.size();

    DataView ret = new DataView(min, max, mean, moods.size(), 0);

    return ret;
  }

  public IReadOnlyList<MoodResult> getList() {
    return moods;
  }
}
