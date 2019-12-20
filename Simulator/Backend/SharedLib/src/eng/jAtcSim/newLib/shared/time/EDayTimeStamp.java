package eng.jAtcSim.newLib.shared.time;

public class EDayTimeStamp extends ETime implements ITimeComparable<EDayTimeStamp>, IDayGetter, ITimeGetter {

  public EDayTimeStamp(int days, int hours, int minutes, int seconds) {
    super(days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
  }
}
