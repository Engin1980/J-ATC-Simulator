/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.Acc;
import jatcsimlib.Acc;
import jatcsimlib.exceptions.ENotSupportedException;
import jatcsimlib.global.TrafficCategories;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Marek
 */
public class AirplaneTypes extends ArrayList<AirplaneType> {

  private int lastRefreshCount = -1;

  private Map<Character, List<AirplaneType>> inner = new HashMap();

  private void rebuild() {
    if (lastRefreshCount == this.size()) {
      return;
    }

    inner.clear();
    inner.put('A', new ArrayList<AirplaneType>());
    inner.put('B', new ArrayList<AirplaneType>());
    inner.put('C', new ArrayList<AirplaneType>());
    inner.put('D', new ArrayList<AirplaneType>());

    for (AirplaneType t : this) {
      inner.get(t.category).add(t);
    }

    lastRefreshCount = this.size();
  }

  public AirplaneType getRandomFromCategory(char category) {
    AirplaneType ret;

    rebuild();
    
    List<AirplaneType> tmp = inner.get(category);
    ret = tmp.get(Acc.rnd().nextInt(tmp.size()));

    return ret;
  }

  public AirplaneType getRandomByTraffic(TrafficCategories traffic, boolean isIfr) {
    AirplaneType ret;
    char c;
    double d = Acc.rnd().nextDouble();

    if (d < traffic.getCategoryA() || !isIfr) { // VFR is allways A
      c = 'A';
    } else if (d < (traffic.getCategoryA() + traffic.getCategoryB())) {
      c = 'B';
    } else if (d < (traffic.getCategoryA() + traffic.getCategoryB() + traffic.getCategoryC())) {
      c = 'C';
    } else {
      c = 'D';
    }

    ret = getRandomFromCategory(c);

    return ret;
  }

}
