package exml.implemented;

import eng.eSystem.eXml.XElement;
import eng.eSystem.functionalInterfaces.Selector;

import java.awt.*;

public class AwtFontDeserializer implements Selector<XElement, java.awt.Font> {
  public final static String ATTR_FAMILY = "family";
  public final static String ATTR_STYLE = "style";
  public final static String ATTR_SIZE = "size";

  @Override
  public Font invoke(XElement element) {
    String familyName = element.getAttributes().get(ATTR_FAMILY);
    String styleS = element.getAttributes().get(ATTR_STYLE);
    String sizeS = element.getAttributes().get(ATTR_SIZE);

    int style = toInt(styleS, ATTR_STYLE);
    int size = toInt(sizeS, ATTR_SIZE);

    Font ret = new Font(familyName, style, size);
    return ret;
  }

  private int toInt(String value, String key) {
    int ret;
    try{
      ret = Integer.parseInt(value);
    } catch (Exception ex){
      throw new RuntimeException("Failed to convert attribute " + key + " value " + value + " to {int} in " + this.getClass().getName() + " parsing.");
    }
    return ret;
  }
}
