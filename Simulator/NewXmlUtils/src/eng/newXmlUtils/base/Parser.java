package eng.newXmlUtils.base;

public interface Parser {
  Object parse(String value);

  default Deserializer toDeserializer() {
    return (e, c) -> this.parse(e.getContent());
  }
}
