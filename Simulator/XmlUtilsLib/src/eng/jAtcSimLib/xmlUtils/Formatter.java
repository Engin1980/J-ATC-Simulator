package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;

public interface Formatter<T> extends Selector<T, String> {
  default Serializer<T> toSerializer(){
    return (XElement e, T t) -> e.setContent(this.invoke(t));
  }
}
