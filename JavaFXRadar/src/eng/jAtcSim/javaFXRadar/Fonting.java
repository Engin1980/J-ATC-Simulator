package eng.jAtcSim.javaFXRadar;

import eng.jAtcSim.radarBase.global.Font;

import java.util.HashMap;

public class Fonting {

  private static HashMap<Font, javafx.scene.text.Font> maps = new HashMap<>();

  public static javafx.scene.text.Font get(Font font){
    if (maps.containsKey(font) == false){
      javafx.scene.text.Font awf = convert(font);
      maps.put(font,awf);
    }

    return maps.get(font);
  }

  private static javafx.scene.text.Font convert(Font font) {
    javafx.scene.text.Font ret = new  javafx.scene.text.Font( font.getName(), font.getSize());
    return ret;
  }

}
