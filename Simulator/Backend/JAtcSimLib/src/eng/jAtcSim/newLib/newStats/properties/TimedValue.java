package eng.jAtcSim.newLib.newStats.properties;

import eng.jAtcSim.newLib.global.ETime;

public class TimedValue<T> {
  private ETime time;
  private T value;

  public TimedValue(ETime time, T value) {
    this.time = time;
    this.value = value;
  }

  public ETime getTime() {
    return time;
  }

  public T getValue() {
    return value;
  }
}
