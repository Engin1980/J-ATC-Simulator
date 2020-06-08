/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.area.airplanes;

import eng.eSystem.collections.*;
import eng.eSystem.eXml.XElement;

/**
 * @author Marek
 */
public class AirplaneTypes {

  public static AirplaneTypes load(XElement root) {

    AirplaneTypes ret = new AirplaneTypes();

    for (XElement child : root.getChildren("type")) {
      AirplaneType at = AirplaneType.load(child);
      ret.allList.add(at);
      ret.typeMap.get(at.category).add(at);
    }

    return ret;
  }

  private final IList<AirplaneType> allList;
  private final IMap<Character, IList<AirplaneType>> typeMap;

  private AirplaneTypes(){
    allList = new EList<>();
    typeMap = new EMap<>();
    typeMap.set('A', new EList<>());
    typeMap.set('B', new EList<>());
    typeMap.set('C', new EList<>());
    typeMap.set('D', new EList<>());

  }

  public AirplaneType tryGetByName(String name) {
    AirplaneType ret = allList.tryGetFirst(q->q.name.equals(name));
    return ret;
  }

  public static AirplaneType getDefaultType() {
    AirplaneType ret = new AirplaneType(
        "A319", "Airbus A319",
        'C', 39000, 145,
        120, 160, 130, 190, 330,
        287, 210,
        5000, 700, 2000, 3500,
        3,3,3    );
    return ret;
  }

  public AirplaneType getRandomFromCategory(char category) {
    AirplaneType ret;
    ret = this.typeMap.get(category).getRandom();
    return ret;
  }

  public AirplaneType getRandom() {
    AirplaneType ret = this.allList.getRandom();
    return ret;
  }
}
