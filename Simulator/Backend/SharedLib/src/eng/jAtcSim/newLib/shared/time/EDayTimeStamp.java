package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.validation.EAssert;

public class EDayTimeStamp extends EDayTime {
  private final int value;

  public EDayTimeStamp(int value) {
    EAssert.Argument.isTrue(value >= 0, "Value must be non-negative.");
    this.value = value;
  }


  public EDayTimeStamp(int days, int hours, int minutes, int seconds) {
    this(days * SECONDS_PER_DAY + hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
  }

  @Override
  public int getValue() {
    return value;
  }
}
