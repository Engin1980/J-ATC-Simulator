package JAtcSim.SwingRadar;

import JAtcSim.radarBase.global.Color;

import java.util.HashMap;

public class Coloring {

  private static HashMap<JAtcSim.radarBase.global.Color, java.awt.Color> maps = new HashMap<>();

  public static java.awt.Color get(JAtcSim.radarBase.global.Color color){
    if (maps.containsKey(color) == false){
      java.awt.Color awc = generateAwtColor(color);
      maps.put(color,awc);
    }

    return maps.get(color);
  }

  private static java.awt.Color generateAwtColor(Color color) {
    java.awt.Color ret = new java.awt.Color(color.getR(), color.getG(), color.getB());
    return ret;
  }

}
