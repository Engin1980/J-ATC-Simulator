package eng.jAtcSim.newLib.shared.time;

import eng.eSystem.utilites.NumberUtils;
import eng.eSystem.validation.EAssert;

public class ETimeOnlyStamp {
  private static final int HOUR_SECONDS = 60 * 60;
  private static final int DAY_SECONDS = HOUR_SECONDS * 24;
  private final int value;

  public ETimeOnlyStamp(int hour, int minute, int second) {
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, hour, 23));
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, minute, 59));
    EAssert.isTrue(NumberUtils.isBetweenOrEqual(0, second, 59));
    this.value = hour * DAY_SECONDS + minute * HOUR_SECONDS + second;
  }

  public int getDays() {
    return value / DAY_SECONDS;
  }

  public int getHours() {
    return value / HOUR_SECONDS;
  }

  public int getMinutes() {
    return value / 60 % 60;
  }

  public int getSeconds() {
    return value % 60;
  }

  public int getValue() {
    return this.value;
  }
}
