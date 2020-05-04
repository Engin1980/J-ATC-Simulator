package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.EMap;
import eng.eSystem.collections.IMap;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.exceptions.ToDoException;
import eng.jAtcSim.newLib.shared.time.EDayTime;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.traffic.movementTemplating.MovementTemplate;

public class TrafficProvider {
  private ITrafficModel trafficModel;
  private IMap<Integer, IReadOnlyList<MovementTemplate>> movementSet = new EMap<>();

  public TrafficProvider(ITrafficModel trafficModel) {
    EAssert.isNotNull(trafficModel);
    this.trafficModel = trafficModel;
  }

  public IReadOnlyList<ExpectedMovement> getExpectedTimesForDay(int dayIndex) {
    throw new ToDoException();
  }

  public IReadOnlyList<MovementTemplate> getMovementsUntilTime(EDayTime untilTime) {
    throw new ToDoException();
//    IList<MovementTemplate> ret = this.movements
//      .where(q->q.getTime().getValue() >= fromTimeInclusive.getValue() && q.getTime().getValue() < toTimeExclusive.getValue());
//    return ret;
  }

  public void init() {
    // intentionally blank
  }

  private void prepareMovementsPerDay(int dayIndex) {
    EAssert.isFalse(movementSet.containsKey(dayIndex), "Set already contains movements for day " + dayIndex);
    IReadOnlyList<MovementTemplate> tmp = trafficModel.generateMovementsForOneDay();
    movementSet.set(dayIndex, tmp);
  }
}
