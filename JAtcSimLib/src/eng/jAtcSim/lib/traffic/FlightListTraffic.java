package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.global.ETime;

public class FlightListTraffic extends Traffic {

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IReadOnlyList<ETime> getExpectedTimesForDay() {
    throw new UnsupportedOperationException();
  }
}
