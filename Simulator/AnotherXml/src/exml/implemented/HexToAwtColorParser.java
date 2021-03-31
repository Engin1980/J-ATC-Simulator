package exml.implemented;

import eng.eSystem.functionalInterfaces.Selector;
import exml.loading.XLoadException;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HexToAwtColorParser implements Selector<String, java.awt.Color> {
  @Override
  public Color invoke(String value) {
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
      throw new RuntimeException("Unable to parse \"" + value + "\" into color.");
    }

    return ret;
  }
}
