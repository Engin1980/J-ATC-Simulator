package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.Constants;
import exml.IXPersistable;
import exml.XContext;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

public class TimedValue<T> implements IXPersistable {
  @XIgnored private EDayTimeStamp time;
  @XIgnored private T value;

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

  @Override
  public void load(XElement elm, XContext ctx) {
    String dt = elm.getAttribute("time");
    this.time = EDayTimeStamp.parse(dt);
    this.value = (T) (Object) Integer.parseInt(elm.getContent());
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
