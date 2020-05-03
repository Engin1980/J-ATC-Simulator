package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;

import java.time.LocalTime;

public class ETimeStamp extends ETime implements ITimeComparable<ETimeStamp>, ITimeGetter {
  public ETimeStamp(int value) {
    super(value);
  }

  public ETimeStamp(int hours, int minutes, int seconds) {
    super(hours * SECONDS_PER_HOUR + minutes * SECONDS_PER_MINUTE + seconds);
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, hours, 23));
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, minutes, 59));
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, seconds, 59));
  }

  public ETimeStamp(LocalTime localTime) {
    this(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
  }

  @Override
  public ETimeStamp clone() {
    return new ETimeStamp(this.getValue());
  }
}
