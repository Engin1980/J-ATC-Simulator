package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.eXml.XElement;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.Constants;
import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

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
  public void save(XElement elm, XSaveContext ctx) {
    elm.setAttribute("time", time.toDayTimeString());
    ctx.saveObject(value, elm);
  }

  @Override
  public void load(XElement elm, XLoadContext ctx) {
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
