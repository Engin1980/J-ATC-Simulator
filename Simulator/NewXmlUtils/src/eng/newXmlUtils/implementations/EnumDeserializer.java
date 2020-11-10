package eng.newXmlUtils.implementations;

import eng.eSystem.eXml.XElement;
import eng.newXmlUtils.XmlContext;
import eng.newXmlUtils.base.Deserializer;

public class EnumDeserializer implements Deserializer {
  private final Class<Enum> enumClass;

  public EnumDeserializer(Class<Enum> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public Object invoke(XElement e, XmlContext c) {
    String s = e.getContent();
    Object ret = Enum.valueOf(this.enumClass, s);
    return ret;
  }
}
