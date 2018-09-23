package eng.jAtcSim.lib.traffic;

import eng.eSystem.collections.IReadOnlyList;

public class FlightListTraffic extends Traffic {

  @Override
  public GeneratedMovementsResponse generateMovements(Object syncObject) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    throw new UnsupportedOperationException();
  }
}
