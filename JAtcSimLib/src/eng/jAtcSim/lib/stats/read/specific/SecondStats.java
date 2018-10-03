package eng.jAtcSim.lib.stats.read.specific;

import eng.jAtcSim.lib.stats.read.shared.DataView;
import eng.jAtcSim.lib.stats.read.shared.MinMaxMeanCountCurrentView;

public class SecondStats {
  private int secondsElapsed;
  private MinMaxMeanCountCurrentView duration;

  public SecondStats(int secondsElapsed, DataView duration) {
    this.secondsElapsed = secondsElapsed;
    this.duration = new MinMaxMeanCountCurrentView(duration);
  }

  public MinMaxMeanCountCurrentView getDuration() {
    return duration;
  }

  public int getSecondsElapsed() {
    return secondsElapsed;
  }

  public static String toTimeString(double seconds){
    String ret;
    int tmp = (int) Math.floor(seconds);
    int hrs = tmp / 3600;
    tmp = tmp % 3600;
    int min = tmp / 60;
    tmp = tmp % 60;
    int sec = tmp;
    if (hrs == 0){
      ret = String.format("%d:%02d", min, sec);
    } else {
      ret = String.format("%d:%02d:%02d",hrs, min, sec);
    }
    return ret;
  }
}
