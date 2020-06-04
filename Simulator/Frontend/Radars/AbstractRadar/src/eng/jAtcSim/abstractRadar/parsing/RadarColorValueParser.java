package eng.jAtcSim.abstractRadar.parsing;

import eng.eXmlSerialization.XmlException;
import eng.eXmlSerialization.serializers.AttributeSerializer;
import eng.jAtcSim.abstractRadar.global.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadarColorValueParser extends AttributeSerializer {
  @Override
  public boolean acceptsType(Class<?> aClass) {
    return Color.class.equals(aClass);
  }

  @Override
  protected String formatValue(Object o) {
    Color value = (Color) o;
    String ret = String.format("#%02X%02X%02X", value.getR(), value.getG(), value.getB());
    return ret;
  }

  @Override
  protected Object parseValue(String value) {
    Color ret = null;
    String ps = "(..)(..)(..)";
    Pattern p = Pattern.compile(ps);

    Matcher m = p.matcher(value);
    if (m.find()) {
      String r = m.group(1);
      String g = m.group(2);
      String b = m.group(3);
      try {
        int ri = Integer.parseInt(r, 16);
        int gi = Integer.parseInt(g, 16);
        int bi = Integer.parseInt(b, 16);
        ret = new Color(ri, gi, bi);
      } finally {
      }
    }
    if (ret == null) {
      throw new XmlException("Unable to parseOld \"" + value + "\" into color.");
    }

    return ret;
  }
}
