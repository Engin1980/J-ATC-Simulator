package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.timeOld.ETimeOnlyStamp;

public abstract class MovementTemplate {
  public enum eKind {
    arrival,
    departure
  }

  private final eKind kind;
  private final ETimeOnlyStamp time;
  private final int delayInMinutes;
  private final EntryExitInfo entryExitInfo;

  public MovementTemplate(eKind kind, ETimeOnlyStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    EAssert.isNotNull(time);
    EAssert.isTrue(delayInMinutes >= 0);
    EAssert.isNotNull(entryExitInfo);

    this.kind = kind;
    this.time = time;
    this.delayInMinutes = delayInMinutes;
    this.entryExitInfo = entryExitInfo;
  }

  public int getDelayInMinutes() {
    return delayInMinutes;
  }

  public EntryExitInfo getEntryExitInfo() {
    return entryExitInfo;
  }

  public eKind getKind() {
    return kind;
  }

  public ETimeOnlyStamp getTime() {
    return time;
  }

  public boolean isArrival() {
    return kind == eKind.arrival;
  }

  public boolean isDeparture() {
    return kind == eKind.departure;
  }
}
