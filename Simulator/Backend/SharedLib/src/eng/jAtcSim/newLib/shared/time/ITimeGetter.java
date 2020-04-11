package eng.jAtcSim.newLib.shared.time;

import java.time.LocalTime;

public interface ITimeGetter extends ITime {

  int SECONDS_PER_MINUTE = 60;
  int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;
  int SECONDS_PER_DAY = ITimeGetter.SECONDS_PER_HOUR * 24;

  default int getHours() {
    return this.getValue() % SECONDS_PER_DAY / SECONDS_PER_HOUR;
  }

  default int getMinutes() {
    return this.getValue() % SECONDS_PER_HOUR / SECONDS_PER_MINUTE;
  }

  default int getSeconds() {
    return this.getValue() % SECONDS_PER_MINUTE;
  }

  default int getTotalMinutes() {
    return getMinutes() + getHours() * 60;
  }

  default LocalTime toLocalTime() {
    return LocalTime.of(getHours(), getMinutes(), getSeconds());
  }

  default String toTimeString() {
    return String.format("%02d:%02d:%02d", getHours(), getMinutes(), getSeconds());
  }
}
