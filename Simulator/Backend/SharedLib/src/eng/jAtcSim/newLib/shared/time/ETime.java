package eng.jAtcSim.newLib.shared.time;

import java.time.LocalTime;
import java.util.Objects;

public abstract class ETime implements Comparable<ETime> {

  private static final int HOUR_SECONDS = 60 * 60;
  private static final int DAY_SECONDS = HOUR_SECONDS * 24;

  public static ETimeStamp getDifference(ETime a, ETime b) {
    int diff = a.getTotalSeconds() - b.getTotalSeconds();
    diff = Math.abs(diff);
    ETimeStamp ret = new ETimeStamp(diff);
    return ret;
  }

  private int value;

  protected void addOneSecond(){
    this.value++;
  }

  public ETime(int value) {
    this.value = value;
  }

  public ETime(int hour, int minute, int second) {
    set(hour, minute, second);
  }

  public ETime(int day, int hour, int minute, int second) {
    set(day, hour, minute, second);
  }

  public ETime(LocalTime localTime) {
    int h = localTime.getHour();
    int m = localTime.getMinute();
    int s = localTime.getSecond();

    set(h, m, s);
  }

  public ETimeStamp addHours(int hours) {
    return new ETimeStamp(this.value + hours * HOUR_SECONDS);
  }

  public ETimeStamp addMinutes(int minutes) {
    return new ETimeStamp(this.getTotalSeconds() + minutes * 60);
  }

  public ETimeStamp addSeconds(int amount) {
    return new ETimeStamp(value + amount);
  }

  @Override
  public int compareTo(ETime o) {
    return Integer.compare(this.value, o.value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ETime eTime = (ETime) o;
    return value == eTime.value;
  }

  public boolean equals(ETime time) {
    if (time == null) return false;
    return this.value == time.value;
  }

  public int getDays() {
    return value / DAY_SECONDS;
  }

  public int getHours() {
    return value / (60 * 60);
  }

  public int getMinutes() {
    return value / 60 % 60;
  }

  public ETimeStamp getRoundedToNextHour() {
    int val = this.value;
    int hrs = val / HOUR_SECONDS;
    hrs++;
    hrs = hrs * HOUR_SECONDS;
    ETimeStamp ret = new ETimeStamp(hrs);
    return ret;
  }

  public int getSeconds() {
    return value % 60;
  }

  public double getTotalHours() {
    return this.value / 3600d;
  }

  public int getTotalMinutes() {
    return getTotalSeconds() / 60;
  }

  public int getTotalSeconds() {
    return value;
  }

  public int getValue() {
    return this.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  public boolean isAfter(ETime otherTime) {
    return this.value > otherTime.value;
  }

  public boolean isAfterOrEq(ETime otherTime) {
    return isAfter(otherTime) || this.value == otherTime.value;
  }

  public boolean isBefore(ETime otherTime) {
    if (otherTime == null) {
      throw new IllegalArgumentException("Value of {otherTime} cannot not be null.");
    }

    return this.value < otherTime.value;
  }

  public boolean isBeforeOrEq(ETime otherTime) {
    return isBefore(otherTime) || this.value == otherTime.value;
  }

  public boolean isBetween(ETime before, ETime after) {
    return this.isAfterOrEq(before) && this.isBeforeOrEq(after);
  }

  public boolean isBetweenStrictly(ETime before, ETime after) {
    return this.isAfter(before) && this.isBefore(after);
  }

  /**
   * Checks if minute fraction is zero.
   *
   * @return true if seconds == 0, false otherwise.
   */
  public boolean isIntegralMinute() {
    return getSeconds() == 0;
  }

  public String toHourMinuteString() {
    return String.format("%d:%02d", this.getHours(), this.getMinutes());
  }

  public LocalTime toLocalTime() {
    return LocalTime.of(this.getHours(), this.getMinutes(), this.getSeconds());
  }

  public String toMinuteSecondString() {
    return String.format("%d:%02d", this.getMinutes(), this.getSeconds());
  }

  @Override
  public String toString() {
    return String.format("%d.%02d:%02d:%02d", getDays(), getHours(), getMinutes(), getSeconds());
  }

  public String toTimeString() {
    return String.format("%02d:%02d:%02d", getHours(), getMinutes(), getSeconds());
  }

  private void set(int hours, int minutes, int seconds) {
    this.value = hours * 60 * 60 + minutes * 60 + seconds;
  }

  private void set(int day, int hours, int minutes, int seconds) {
    this.value = day * DAY_SECONDS + hours * 60 * 60 + minutes * 60 + seconds;
  }
}
