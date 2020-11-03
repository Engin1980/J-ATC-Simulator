package eng.jAtcSimLib.xmlUtils;

public interface Parser {
  Object parse(String value);

  default Deserializer toDeserializer() {
    return e -> this.parse(e.getContent());
  }
}
