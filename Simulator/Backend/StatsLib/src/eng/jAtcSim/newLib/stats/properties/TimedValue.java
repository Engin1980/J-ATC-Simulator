package eng.jAtcSim.newLib.stats.properties;

import eng.eSystem.eXml.XElement;
import eng.eSystem.exceptions.EEnumValueUnsupportedException;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.IXPersistable;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;
import exml.loading.XLoadContext;
import exml.saving.XSaveContext;

public class TimedValue<T> implements IXPersistable {
  @XIgnored private EDayTimeStamp time;
  @XIgnored private T value;

  @XConstructor
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

  @Override
  public void xLoad(XElement elm, XLoadContext ctx) {
    String valueType = elm.getAttribute("type");
    Object value;
    switch (valueType) {
      case "int":
        value = Integer.parseInt(elm.getContent());
        break;
      case "double":
        value = Double.parseDouble(elm.getContent());
        break;
      default:
        throw new EEnumValueUnsupportedException(valueType);
    }

    String dt = elm.getAttribute("time");
    this.time = EDayTimeStamp.parse(dt);
    this.value = (T) value;
  }

  @Override
  public void xSave(XElement elm, XSaveContext ctx) {
    if (this.getValue() instanceof Integer)
      elm.setAttribute("type", "int");
    else if (this.getValue() instanceof Double)
      elm.setAttribute("type", "double");
    elm.setAttribute("time", time.toDayTimeString());
    elm.setContent(this.value.toString());
  }
}
