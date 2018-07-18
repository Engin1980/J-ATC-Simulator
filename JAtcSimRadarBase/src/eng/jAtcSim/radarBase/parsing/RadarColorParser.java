package eng.jAtcSim.radarBase.parsing;

import eng.eSystem.xmlSerialization.XmlDeserializationException;
import eng.jAtcSim.radarBase.global.Color;
import eng.eSystem.xmlSerialization.IValueParser;
import eng.eSystem.xmlSerialization.XmlSerializationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RadarColorParser implements IValueParser<Color> {
  @Override
  public Class getType() {
    return Color.class;
  }

  @Override
  public Color parse(String value)  throws XmlDeserializationException{
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
      throw new XmlDeserializationException("Unable to parseOld \"" + value + "\" into color.");
    }

    return ret;
  }

  @Override
  public String format(Color value) {
    String ret = String.format("#%02X%02X%02X", value.getR(), value.getG(), value.getB());
    return ret;
  }
}
