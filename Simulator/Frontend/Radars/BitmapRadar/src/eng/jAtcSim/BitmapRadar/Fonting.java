package eng.jAtcSim.BitmapRadar;

import eng.jAtcSim.abstractRadar.global.Font;

import java.util.HashMap;

public class Fonting {

  private static HashMap<Font, java.awt.Font> maps = new HashMap<>();

  public static java.awt.Font get(Font font){
    if (maps.containsKey(font) == false){
      java.awt.Font awf = convert(font);
      maps.put(font,awf);
    }

    return maps.get(font);
  }

  private static java.awt.Font convert(Font font) {
    java.awt.Font ret = new java.awt.Font(font.getName(), font.getStyle(), font.getSize());
    return ret;
  }

}
