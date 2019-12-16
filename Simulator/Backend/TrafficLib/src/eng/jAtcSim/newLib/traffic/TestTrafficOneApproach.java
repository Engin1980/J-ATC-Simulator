/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.ArrivalEntryInfo;
import eng.jAtcSim.newLib.traffic.movementTemplating.ArrivalMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.IMovementTemplate;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

/**
 * @author Marek Vajgl
 */
public class TestTrafficOneApproach extends TestTraffic {

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    return new EList<>();
  }

  @Override
  public IReadOnlyList<IMovementTemplate> getMovements(ETimeStamp fromTimeInclusive, ETimeStamp toTimeExclusive) {
    IList<IMovementTemplate> ret = new EList<>();
    ETimeStamp time;
    if (fromTimeInclusive.getMinutes() == 0 && fromTimeInclusive.getSeconds() == 0)
      time = fromTimeInclusive;
    else
      time = new ETimeStamp(fromTimeInclusive.getDays(), fromTimeInclusive.getHours(), fromTimeInclusive.getMinutes(), fromTimeInclusive.getSeconds());
    while (time.isBefore(toTimeExclusive)) {
      MovementTemplate tmp = new ArrivalMovementTemplate(
          new Callsign("CSA", time.getHours() + "00"),
          TestTraffic.AIRPLANE_TYPE,
          time,
          0,
          new ArrivalEntryInfo(290)
      );
      ret.add(tmp);
      time = time.addHours(1);
    }
    return ret;
  }
}
