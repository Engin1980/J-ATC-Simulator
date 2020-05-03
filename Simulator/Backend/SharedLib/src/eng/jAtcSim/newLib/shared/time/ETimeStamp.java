package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.validation.EAssert;

import java.time.LocalTime;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public class ETimeStamp implements ITime, ITimeComparable<ETimeStamp> {

  private final int value;

  public ETimeStamp(int value) {
    EAssert.Argument.isTrue(value >= 0, "Number of seconds must be non-negative.");
    EAssert.Argument.isTrue(value < ITime.SECONDS_PER_DAY, "Number of seconds must be less than one day.");
    this.value = value;
  }

  public ETimeStamp(int hours, int minutes, int seconds) {
    this(hours * ITime.SECONDS_PER_HOUR + minutes * ITime.SECONDS_PER_MINUTE + seconds);
  }

  public ETimeStamp(LocalTime time) {
    this(time.getHour(), time.getMinute(), time.getSecond());
  }

  @Override
  public int getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return toTimeString();
  }
}
