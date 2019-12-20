package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.timeOld.ETimeOnlyStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public interface ITraffic {
  IReadOnlyList<ExpectedMovement> getExpectedTimesForDay();

  IReadOnlyList<MovementTemplate> getMovements(
      ETimeOnlyStamp fromTimeInclusive, ETimeOnlyStamp toTimeExclusive);
}
