package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class EDayTimeRun extends EDayTime {
  private int value;

  public EDayTimeRun(int value) {
    EAssert.Argument.isTrue(value >= 0, "Value must be non-negative.");
    this.value = value;
  }

  public EDayTimeRun(int days, int hours, int minutes, int seconds) {
    this(days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
  }

  public void increaseSecond() {
    this.value++;
  }

  @Override
  public int getValue() {
    return value;
  }

  public EDayTimeStamp toStamp() {
    return new EDayTimeStamp(this.getValue());
  }
}
