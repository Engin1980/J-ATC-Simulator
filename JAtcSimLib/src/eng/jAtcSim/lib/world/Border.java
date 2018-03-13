/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eng.jAtcSim.lib.world;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marek
 */
public class Border {
  public enum eType{
    Country,
    TMA,
    CTR,
    Restricted,
    Shore,
    Other
  }
  
  private String name;
  private eType type;
  private final List<BorderPoint> points = new ArrayList();
  private boolean enclosed;

  public String getName() {
    return name;
  }

  public eType getType() {
    return type;
  }

  public List<BorderPoint> getPoints() {
    return points;
  }

  public boolean isEnclosed() {
    return enclosed;
  }
  
  
}
