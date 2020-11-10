package eng.newXmlUtils.base;

import eng.eSystem.functionalInterfaces.Selector;

public interface Formatter<T> extends Selector<T, String> {
  default Serializer toSerializer(){
    return (e, v, c) -> e.setContent(this.invoke((T) v));
  }
}
