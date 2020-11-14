package eng.newXmlUtils.base;

import eng.eSystem.functionalInterfaces.Selector2;
import eng.newXmlUtils.XmlContext;

public interface Parser<T> extends Selector2<String, XmlContext, T> {
  default Deserializer toDeserializer() {
    return (e, c) -> this.invoke(e.getContent(), c);
  }
}
