package eng.jAtcSim.newLib.shared.time;

import java.time.LocalTime;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public abstract class EDayTime implements ITime, ITimeComparable<EDayTime> {
  public EDayTimeStamp addHours(int hours) {
    return this.addSeconds(hours * MINUTES_PER_HOUR *ITime.SECONDS_PER_MINUTE);
  }

  public EDayTimeStamp addMinutes(int minutes) {
    return this.addSeconds(minutes * ITime.SECONDS_PER_MINUTE);
  }

  public EDayTimeStamp addSeconds(int seconds) {
    return new EDayTimeStamp(this.getValue() + seconds);
  }

  public int getDays() {
    return this.getValue() / ITime.SECONDS_PER_DAY;
  }

  public ETimeStamp getTime() {
    return new ETimeStamp(getHours(), getMinutes(), getSeconds());
  }

  public String toDayTimeString() {
    return sf("%d.%02d:%02d:%02d",
        this.getDays(),
        this.getHours(),
        this.getMinutes(),
        this.getSeconds());
  }

  @Override
  public String format(){
    return toDayTimeString();
  }



  public LocalTime toLocalTime() {
    return LocalTime.of(this.getDays(), this.getMinutes(), this.getSeconds());
  }

  @Override
  public String toString() {
    return this.toDayTimeString();
  }
}
