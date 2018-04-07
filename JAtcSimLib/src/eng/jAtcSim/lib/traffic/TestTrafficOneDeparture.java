/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.lib.traffic;

import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.airplanes.AirplaneType;
import eng.jAtcSim.lib.airplanes.Callsign;

/**
 *
 * @author Marek Vajgl
 */
public class TestTrafficOneDeparture extends TestTraffic {

  boolean done = false;

  private Movement generateMovement(String number) {

    Movement ret;

    Callsign cs;
    cs = new Callsign("CSA", number);
    AirplaneType pt = Acc.sim().getPlaneTypes().tryGetByName("A319");
    assert pt != null;

    ret = new Movement(cs, pt, Acc.now().clone(), 0, true);

    return ret;
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (!done) {
      super.addScheduledMovement(generateMovement("0000"));
//      super.addScheduledMovement(generateMovement("5555"));
//      super.addScheduledMovement(generateMovement("7777"));
      done = true;
    }
  }

}
