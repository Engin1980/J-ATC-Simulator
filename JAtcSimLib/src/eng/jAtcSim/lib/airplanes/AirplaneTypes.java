/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.ECollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marek
 */
public class AirplaneTypes extends ArrayList<AirplaneType> {

  private int lastRefreshCount = -1;

  private Map<Character, List<AirplaneType>> inner = new HashMap();

  public AirplaneType tryGetByTypeName(String typeName) {
    AirplaneType ret = ECollections.tryGetFirst(this, o -> o.name.equals(typeName));
    return ret;
  }

  public static AirplaneType getDefaultType() {
    AirplaneType ret = new AirplaneType();
    ret.name = "A319";
    ret.category = 'C';
    ret.maxAltitude = 39000;
    ret.vMinApp = 120;
    ret.vApp = 130;
    ret.vMaxApp = 160;
    ret.vMinClean = 190;
    ret.vMaxClean = 330;
    ret.vCruise = 287;
    ret.lowClimbRate = 5000;
    ret.highClimbRate = 700;
    ret.lowDescendRate = 2000;
    ret.highDescendRate = 3500;
    ret.speedIncreaseRate = 3;
    ret.speedDecreaseRate = 3;
    ret.headingChangeRate = 3;
    return ret;
  }

  public AirplaneType getRandomFromCategory(char category) {
    AirplaneType ret;

    rebuild();

    List<AirplaneType> tmp = inner.get(category);
    ret = tmp.get(Acc.rnd().nextInt(tmp.size()));

    return ret;
  }

  public AirplaneType tryGetByName(String name) {
    AirplaneType ret = ECollections.tryGetFirst(this, o -> o.name.equals(name));
    return ret;
  }

  public AirplaneType getRandom() {
    AirplaneType ret = ECollections.getRandom(this);
    return ret;
  }

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


}
