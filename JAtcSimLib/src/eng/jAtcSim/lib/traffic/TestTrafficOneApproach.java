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
public class TestTrafficOneApproach extends TestTraffic {

  boolean done = false;

  private Movement generateMovement() {
    Movement ret;

    AirplaneType pt = Acc.sim().getPlaneTypes().tryGetByName("A319");
    assert pt != null;

    ret = new Movement(
      new Callsign("CSA", "1111"),
      pt,
        Acc.now().clone(), 0, false);

    return ret;
  }

  @Override
  public void generateNewMovementsIfRequired() {
    if (!done) {
      super.addScheduledMovement(generateMovement());
      done = true;
    }
  }
}
