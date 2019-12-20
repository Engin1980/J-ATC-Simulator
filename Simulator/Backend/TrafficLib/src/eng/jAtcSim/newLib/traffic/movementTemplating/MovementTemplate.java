package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public abstract class MovementTemplate {
  public enum eKind {
    arrival,
    departure
  }

  private final eKind kind;
  private final ETimeStamp time;
  private final EntryExitInfo entryExitInfo;

  public MovementTemplate(eKind kind, ETimeStamp time, EntryExitInfo entryExitInfo) {
    EAssert.isNotNull(time);
    EAssert.isNotNull(entryExitInfo);

    this.kind = kind;
    this.time = time;
    this.entryExitInfo = entryExitInfo;
  }

  public EntryExitInfo getEntryExitInfo() {
    return entryExitInfo;
  }

  public eKind getKind() {
    return kind;
  }

  public ETimeStamp getTime() {
    return time;
  }

  public boolean isArrival() {
    return kind == eKind.arrival;
  }

  public boolean isDeparture() {
    return kind == eKind.departure;
  }
}
