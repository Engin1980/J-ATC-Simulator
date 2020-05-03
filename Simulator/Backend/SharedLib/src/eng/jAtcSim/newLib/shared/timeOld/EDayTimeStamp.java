package eng.jAtcSim.newLib.shared.time;

public class EDayTimeStamp extends ETime implements ITimeComparable<EDayTimeStamp>, IDayGetter, ITimeGetter {

  public EDayTimeStamp(int days, int hours, int minutes, int seconds) {
    super(days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
  }

  public EDayTimeStamp(int value) {
    super(value);
  }

  public EDayTimeStamp addMinutes(int minutes) {
    return this.addSeconds(minutes * 60);
  }

  public EDayTimeStamp addSeconds(int seconds) {
    return new EDayTimeStamp(this.getValue() + seconds);
  }
}
