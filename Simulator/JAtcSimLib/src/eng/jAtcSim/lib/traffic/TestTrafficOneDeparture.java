/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.xmlSerialization.annotations.XmlIgnore;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.global.ETime;

/**
 * @author Marek Vajgl
 */
public class TestTrafficOneDeparture extends TestTraffic {

  @XmlIgnore
  private String[] clsgnNumbers = new String[]{"5555" }; //, "6666", "7777"};
  @XmlIgnore
  private String[] types = new String[]{"A319", "A319", "A319"};

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    Boolean done = (Boolean) syncObject;
    IList<Movement> lst = new EList<>();
    if (done == null || done == false) {
      Movement mov;
      for (int i = 0; i < clsgnNumbers.length; i++) {
        String clsgnNumber = clsgnNumbers[i];
        String type = types[i];
        mov = generateMovement(clsgnNumber, type);
        lst.add(mov);
      }
    }

    GeneratedMovementsResponse ret = new GeneratedMovementsResponse(Acc.now().addHours(10), true, lst);
    return ret;
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    return new EList<>();
  }

  private Movement generateMovement(String number, String typeName) {
    Movement tmp;

    Callsign cs;
    cs = new Callsign("CSA", number);
    AirplaneType pt = Acc.sim().getAirplaneTypes().tryGetByName(typeName);
    assert pt != null;

    tmp = new Movement(cs, pt, Acc.now().clone(), 0, true, 180);
    return tmp;
  }
}
