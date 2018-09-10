package eng.jAtcSim.radarBase.parsing;

import eng.eSystem.eXml.XElement;
import eng.eSystem.xmlSerialization.XmlSerializer;
import eng.eSystem.xmlSerialization.exceptions.XmlSerializationException;
import eng.eSystem.xmlSerialization.supports.IElementParser;
import eng.jAtcSim.radarBase.global.Font;

public class RadarFontParser implements IElementParser<Font> {

  public final static String ATTR_FAMILY = "name";
  public final static String ATTR_STYLE = "style";
  public final static String ATTR_SIZE = "size";

  @Override
  public Font parse(XElement element, XmlSerializer.Deserializer xmlSerializer) {
    String familyName = getAttributeValue(element, ATTR_FAMILY);
    String styleS = getAttributeValue(element, ATTR_STYLE);
    String sizeS = getAttributeValue(element, ATTR_SIZE);

    int style = toInt(styleS, ATTR_STYLE);
    int size = toInt(sizeS, ATTR_SIZE);

    Font ret = new Font(familyName, size, style);
    return ret;
  }

  @Override
  public void format(Font value, XElement element, XmlSerializer.Serializer xmlSerializer) {
    element.setAttribute(ATTR_FAMILY, value.getName());
    element.setAttribute(ATTR_STYLE, Integer.toString(value.getStyle()));
    element.setAttribute(ATTR_SIZE, Integer.toString(value.getSize()));
  }

  private int toInt(String value, String key) {
    int ret;
    try {
      ret = Integer.parseInt(value);
    } catch (Exception ex) {
      throw new XmlSerializationException("Failed to convert attribute " + key + " value " + value + " to {int} in " + this.getClass().getName() + " parsing.");
    }
    return ret;
  }

  private String getAttributeValue(XElement el, String key) {
    if (el.getAttributes().containsKey(key) == false)
      throw new XmlSerializationException("Failed to find required attribute " + key + " for " + this.getClass().getName() + " parsing.");
    String ret = el.getAttributes().get(key);
    return ret;
  }
}