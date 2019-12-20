package eng.jAtcSim.newLib.shared.time;

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
}
