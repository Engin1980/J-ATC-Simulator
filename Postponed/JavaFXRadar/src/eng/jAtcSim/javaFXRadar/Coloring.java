package eng.jAtcSim.javaFXRadar;

import eng.jAtcSim.abstractRadar.global.Color;

import java.util.HashMap;

public class Coloring {

  private static HashMap<Color, javafx.scene.paint.Color> maps = new HashMap<>();

  public static javafx.scene.paint.Color get(Color color){
    if (maps.containsKey(color) == false){
      javafx.scene.paint.Color awc = generateFxColor(color);
      maps.put(color,awc);
    }

    return maps.get(color);
  }

  private static javafx.scene.paint.Color generateFxColor(Color color) {
    javafx.scene.paint.Color ret = new javafx.scene.paint.Color(color.getR(), color.getG(), color.getB(), 0);
    return ret;
  }

}
