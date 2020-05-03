package eng.jAtcSim.newLib.shared.time;

public interface ITime {
  int HOURS_PER_DAY = 24;
  int SECONDS_PER_DAY = ITimeGetter.SECONDS_PER_HOUR * HOURS_PER_DAY;
  int SECONDS_PER_MINUTE = 60;
  int SECONDS_PER_HOUR = SECONDS_PER_MINUTE * 60;

  default int getHours() {
    return this.getValue() % SECONDS_PER_DAY / SECONDS_PER_HOUR;
  }

  default int getMinutes() {
    return this.getValue() % SECONDS_PER_HOUR / SECONDS_PER_MINUTE;
  }

  default int getSeconds() {
    return this.getValue() % SECONDS_PER_MINUTE;
  }

  int getValue();
}
