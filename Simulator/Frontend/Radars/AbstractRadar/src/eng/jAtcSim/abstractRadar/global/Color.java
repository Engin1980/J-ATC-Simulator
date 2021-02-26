package eng.jAtcSim.abstractRadar.global;

import eng.eXmlSerialization.XmlException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {
  public static final Color MAGENTA = new Color(0xFF, 0x0, 0xFF);

  public static Color fromHex(String hex) {
    Color ret;
    String ps = "([0-9A-Fa-f][0-9A-Fa-f])([0-9A-Fa-f][0-9A-Fa-f])([0-9A-Fa-f][0-9A-Fa-f])";
    Pattern p = Pattern.compile(ps);

    Matcher m = p.matcher(hex);
    if (m.find()) {
      String r = m.group(1);
      String g = m.group(2);
      String b = m.group(3);
      try {
        int ri = Integer.parseInt(r, 16);
        int gi = Integer.parseInt(g, 16);
        int bi = Integer.parseInt(b, 16);
        ret = new Color(ri, gi, bi);
      } catch (Exception ex) {
        throw new XmlException("Unable to parse \"" + hex + "\" into color.");
      }
    } else
      throw new XmlException("Unable to parse \"" + hex + "\" into color; value does not represent color.");

    return ret;
  }

  private final int r;
  private final int g;
  private final int b;

  public Color() {
    this.r = 0;
    this.g = 0;
    this.b = 0;
  }

  public Color(int r, int g, int b) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public int getB() {
    return b;
  }

  public int getG() {
    return g;
  }

  public int getR() {
    return r;
  }

  @Override
  public String toString() {
    return
            String.format("RadarColor{%d,%d,%d}", r, g, b);
  }
}
