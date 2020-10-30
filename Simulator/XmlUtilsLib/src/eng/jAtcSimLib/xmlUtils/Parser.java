package eng.jAtcSimLib.xmlUtils;

import eng.eSystem.eXml.XElement;

public interface Parser {
  Object parse(String value);

  default Deserializer toDeserializer(){
    return (e, q) -> this.parse(e.getContent());
  }
}
