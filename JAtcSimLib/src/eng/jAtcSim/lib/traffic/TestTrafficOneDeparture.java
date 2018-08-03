/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;

/**
 * @author Marek Vajgl
 */
public class TestTrafficOneDeparture extends TestTraffic {

  private String[] clsgnNumbers = new String[]{"5555", "6666", "7777"};

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

  private static String typeName = "C152";

  private Movement generateMovement(String number) {
    Movement tmp;

    Callsign cs;
    cs = new Callsign("CSA", number);
    AirplaneType pt = Acc.sim().getAirplaneTypes().tryGetByName(typeName);
    assert pt != null;

    typeName = "C172";

    tmp = new Movement(cs, pt, Acc.now().clone(), 0, true, 180);
    return tmp;
  }
}
