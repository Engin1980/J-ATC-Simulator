package eng.jAtcSim.newLib.traffic;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneratedMovementsResponse {
  private ETimeStamp nextTime;
  private Object syncObject;
  private IReadOnlyList<Movement> newMovements;

  public GeneratedMovementsResponse(ETimeStamp nextTime, Object syncObject, IReadOnlyList<Movement> newMovements) {
    this.nextTime = nextTime;
    this.syncObject = syncObject;
    this.newMovements = newMovements;
  }

  public ETimeStamp getNextTime() {
    return nextTime;
  }

  public Object getSyncObject() {
    return syncObject;
  }

  public IReadOnlyList<Movement> getNewMovements() {
    return newMovements;
  }
}
