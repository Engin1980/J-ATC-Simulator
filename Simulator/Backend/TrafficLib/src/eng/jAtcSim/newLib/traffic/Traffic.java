package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeOnlyStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public abstract class Traffic implements ITraffic {
  private final IReadOnlyList<MovementTemplate> movements;

  @Override
  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay() {
    return null;
  }

  @Override
  public IReadOnlyList<MovementTemplate> getMovements(ETimeOnlyStamp fromTimeInclusive, ETimeOnlyStamp toTimeExclusive) {
    IList<MovementTemplate> ret = this.movements
      .where(q->q.getTime().getValue() >= fromTimeInclusive.getValue() && q.getTime().getValue() < toTimeExclusive.getValue());
    return ret;
  }

  public Traffic(IReadOnlyList<MovementTemplate> movements) {
    EAssert.isNotNull(movements);
    this.movements = movements;
  }
}
