package eng.jAtcSim.radarBase.parsing;

import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.jAtcSim.radarBase.global.Font;
import eng.eSystem.xmlSerialization.IElementParser;
import eng.eSystem.xmlSerialization.XmlSerializationException;
import org.w3c.dom.Element;

public class RadarFontParser implements IElementParser<Font> {

  public final static String ATTR_FAMILY = "name";
  public final static String ATTR_STYLE = "style";
  public final static String ATTR_SIZE = "size";

  @Override
  public Class getType() {
    return Font.class;
  }

  @Override
  public Font parse(Element element)  throws XmlDeserializationException{
    String familyName = getAttributeValue(element, ATTR_FAMILY);
    String styleS = getAttributeValue(element, ATTR_STYLE);
    String sizeS = getAttributeValue(element, ATTR_SIZE);

    int style = toInt(styleS, ATTR_STYLE);
    int size = toInt(sizeS, ATTR_SIZE);

    Font ret = new Font(familyName, size, style);
    return ret;
  }

  @Override
  public void format(Font value, Element element) {
    element.setAttribute(ATTR_FAMILY, value.getName());
    element.setAttribute(ATTR_STYLE, Integer.toString(value.getStyle()));
    element.setAttribute(ATTR_SIZE, Integer.toString(value.getSize()));
  }

  private int toInt(String value, String key) throws XmlDeserializationException {
    int ret;
    try {
      ret = Integer.parseInt(value);
    } catch (Exception ex) {
      throw new XmlDeserializationException("Failed to convert attribute " + key + " value " + value + " to {int} in " + this.getClass().getName() + " parsing.");
    }
    return ret;
  }

  private String getAttributeValue(Element el, String key)throws XmlDeserializationException {
    if (el.hasAttribute(key) == false)
      throw new XmlDeserializationException("Failed to find required attribute " + key + " for " + this.getClass().getName() + " parsing.");
    String ret = el.getAttribute(key);
    return ret;
  }
}