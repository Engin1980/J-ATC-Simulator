package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.global.ETime;

public class GeneratedMovementsResponse {
  private ETime nextTime;
  private Object syncObject;
  private IReadOnlyList<Movement> newMovements;

  public GeneratedMovementsResponse(ETime nextTime, Object syncObject, IReadOnlyList<Movement> newMovements) {
    this.nextTime = nextTime;
    this.syncObject = syncObject;
    this.newMovements = newMovements;
  }

  public ETime getNextTime() {
    return nextTime;
  }

  public Object getSyncObject() {
    return syncObject;
  }

  public IReadOnlyList<Movement> getNewMovements() {
    return newMovements;
  }
}
