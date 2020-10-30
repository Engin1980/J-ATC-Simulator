package eng.jAtcSimLib.xmlUtils.deserializers;

import eng.eSystem.eXml.XElement;
import eng.jAtcSimLib.xmlUtils.Deserializer;

public class IterableDeserializer implements Deserializer {
  private final Class<?> componentType;
  private final Deserializer itemDeserializer;

  public IterableDeserializer(Class<?> componentType, Deserializer itemDeserializer) {
    this.componentType = componentType;
    this.itemDeserializer = itemDeserializer;
  }

  @Override
  public Object deserialize(XElement element, Class<?> type) {
    fasef dopsat tady
  }
}
