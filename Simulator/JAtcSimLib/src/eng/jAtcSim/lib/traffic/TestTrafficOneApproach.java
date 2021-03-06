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
public class TestTrafficOneApproach extends TestTraffic {

  @XmlIgnore
  private String[] clsgnNumbers = new String[]{"5555"}; //, "6666", "7777"};

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    Boolean done = (Boolean) syncObject;
    IList<Movement> lst = new EList<>();
    if (done == null || done == false) {
      Movement mov;
      for (String clsgnNumber : clsgnNumbers) {
        mov = generateMovement(clsgnNumber);
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

  private Movement generateMovement(String number) {
    Movement ret;

    AirplaneType pt = Acc.sim().getAirplaneTypes().tryGetByName("A319");
    assert pt != null;

    ret = new Movement(
        new Callsign("CSA", number),
        pt,
        Acc.now().clone(), 0, false, 290);

    return ret;
  }
}
