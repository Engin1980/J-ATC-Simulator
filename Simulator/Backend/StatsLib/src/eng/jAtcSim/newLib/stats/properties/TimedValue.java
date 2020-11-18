package eng.jAtcSim.newLib.stats.properties;

import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

public class TimedValue<T> {
  private final EDayTimeStamp time;
  private final T value;

  @XmlConstructor
  private TimedValue() {
    this.time = null;
    this.value = null;
  }

  public TimedValue(EDayTimeStamp time, T value) {
    this.time = time;
    this.value = value;
  }

  //TODO rename to getDayTime()
  public EDayTimeStamp getTime() {
    return time;
  }

  public T getValue() {
    return value;
  }
}
