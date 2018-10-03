package eng.jAtcSim.lib.stats.write.specific;

import eng.jAtcSim.lib.stats.write.shared.Record;

public class SecondStats {
  private Record duration = new Record();

  public Record getDuration() {
    return duration;
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
