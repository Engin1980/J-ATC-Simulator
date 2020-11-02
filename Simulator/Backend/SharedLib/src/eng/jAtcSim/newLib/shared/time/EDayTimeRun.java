package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.utilites.RegexUtils;
import eng.eSystem.validation.EAssert;

public class EDayTimeRun extends EDayTime {
  public static EDayTimeRun parse(String value) {
    String pattern = "(\\d)\\.(\\d{2}):(\\d{2}):(\\d{2})";

    String[] groups = RegexUtils.extractGroups(value, pattern);

    int day = Integer.parseInt(groups[1]);
    int hour = Integer.parseInt(groups[2]);
    int minute = Integer.parseInt(groups[3]);
    int second = Integer.parseInt(groups[4]);

    return new EDayTimeRun(day, hour, minute, second);
  }

  private int value;

  public EDayTimeRun(int value) {
    EAssert.Argument.isTrue(value >= 0, "Value must be non-negative.");
    this.value = value;
  }

  public EDayTimeRun(int days, int hours, int minutes, int seconds) {
    this(days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
  }

  @Override
  public int getValue() {
    return value;
  }

  public void increaseSecond() {
    this.value++;
  }

  public EDayTimeStamp toStamp() {
    return new EDayTimeStamp(this.getValue());
  }
}
