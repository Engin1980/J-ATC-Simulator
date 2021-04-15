package eng.jAtcSim.newPacks.layout;

import java.awt.*;

class ColorProvider {
  private static final Color[] colors = {
          Color.CYAN,
          Color.BLUE,
          Color.DARK_GRAY,
          Color.GREEN,
          Color.MAGENTA,
          Color.ORANGE,
          Color.PINK,
          Color.RED,
          Color.WHITE,
          Color.YELLOW
  };
  private static int index = 0;

  public static Color nextColor() {
    Color ret = colors[index];
    index++;
    if (index >= colors.length) index = 0;
    return ret;
  }
}
