package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class TimedValue<T> implements IXPersistable {
  @XIgnored private final EDayTimeStamp time;
  @XIgnored private final T value;

  @XConstructor
  @XmlConstructor
  private TimedValue() {
    this.time = null;
    this.value = null;
  }

  @Override
  public void save(XElement elm, XContext ctx) {
    elm.setAttribute("time", time.toDayTimeString());
    ctx.saver.saveObject(value, elm);
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
