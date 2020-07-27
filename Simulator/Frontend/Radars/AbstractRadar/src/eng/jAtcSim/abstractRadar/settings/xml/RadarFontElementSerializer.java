package eng.jAtcSim.abstractRadar.settings.xml;

import eng.eSystem.eXml.XElement;
import eng.eXmlSerialization.XmlException;
import eng.eXmlSerialization.meta.GenericParameterXmlRuleList;
import eng.eXmlSerialization.serializers.ElementSerializer;
import eng.jAtcSim.abstractRadar.global.Font;

public class RadarFontElementSerializer extends ElementSerializer {

  private final static String ATTR_FAMILY = "family";
  private final static String ATTR_SIZE = "size";
  private final static String ATTR_STYLE = "style";

  @Override
  public boolean acceptsType(Class<?> aClass) {
    return Font.class.equals(aClass);
  }

  @Override
  protected Object _deserialize(XElement element, Class<?> aClass, GenericParameterXmlRuleList genericParameterXmlRuleList) {
    String familyName = getAttributeValue(element, ATTR_FAMILY);
    String styleS = getAttributeValue(element, ATTR_STYLE);
    String sizeS = getAttributeValue(element, ATTR_SIZE);

    int style = toInt(styleS, ATTR_STYLE);
    int size = toInt(sizeS, ATTR_SIZE);

    Font ret = new Font(familyName, size, style);
    return ret;
  }

  @Override
  protected void _serialize(Object o, XElement element, GenericParameterXmlRuleList genericParameterXmlRuleList) {
    Font value = (Font) o;
    element.setAttribute(ATTR_FAMILY, value.getFamily());
    element.setAttribute(ATTR_STYLE, Integer.toString(value.getStyle()));
    element.setAttribute(ATTR_SIZE, Integer.toString(value.getSize()));
  }

  private String getAttributeValue(XElement el, String key) {
    if (el.getAttributes().containsKey(key) == false)
      throw new XmlException("Failed to find required attribute " + key + " for " + this.getClass().getName() + " parsing.");
    String ret = el.getAttributes().get(key);
    return ret;
  }

  private int toInt(String value, String key) {
    int ret;
    try {
      ret = Integer.parseInt(value);
    } catch (Exception ex) {
      throw new XmlException("Failed to convert attribute " + key + " value " + value + " to {int} in " + this.getClass().getName() + " parsing.");
    }
    return ret;
  }
}