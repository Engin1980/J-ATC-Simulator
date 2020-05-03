package eng.jAtcSim.newLib.shared.time;

import static eng.eSystem.utilites.FunctionShortcuts.sf;

public interface ITime {
  int HOURS_PER_DAY = 24;
  int MINUTES_PER_HOUR = 60;
  int SECONDS_PER_MINUTE = 60;
  int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  int SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;

  default int getHours() {
    return this.getValue() % SECONDS_PER_DAY / MINUTES_PER_HOUR;
  }

  default int getMinutes() {
    return this.getValue() % SECONDS_PER_HOUR / SECONDS_PER_MINUTE;
  }

  default int getSeconds() {
    return this.getValue() % SECONDS_PER_MINUTE;
  }

  int getValue();

  default String toHourMinuteString(){
    return sf("%02d:%02d",
        this.getHours(), this.getMinutes());
  }

  default String toTimeString(){
    return sf("%d:%02d:%02d",
        this.getHours(), this.getMinutes(), this.getSeconds());
  }
}
