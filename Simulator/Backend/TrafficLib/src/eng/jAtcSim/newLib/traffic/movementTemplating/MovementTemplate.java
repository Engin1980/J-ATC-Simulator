package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.annotations.XConstructor;

public abstract class MovementTemplate implements IXPersistable {
  public enum eKind {
    arrival,
    departure
  }

  private final eKind kind;
  private final ETimeStamp appearanceTime;
  private final EntryExitInfo entryExitInfo;

  @XConstructor
  @XmlConstructor
  protected MovementTemplate() {
    kind = eKind.arrival;
    appearanceTime = null;
    entryExitInfo = null;
    PostContracts.register(this, () -> this.appearanceTime != null, "appereanceTime");
    PostContracts.register(this, () -> this.entryExitInfo != null, "entryExitInfo");
  }

  public MovementTemplate(eKind kind, ETimeStamp appearanceTime, EntryExitInfo entryExitInfo) {
    EAssert.isNotNull(appearanceTime);
    EAssert.isNotNull(entryExitInfo);

    this.kind = kind;
    this.appearanceTime = appearanceTime;
    this.entryExitInfo = entryExitInfo;
  }

  public ETimeStamp getAppearanceTime() {
    return appearanceTime;
  }

  public EntryExitInfo getEntryExitInfo() {
    return entryExitInfo;
  }

  public eKind getKind() {
    return kind;
  }

  public boolean isArrival() {
    return kind == eKind.arrival;
  }

  public boolean isDeparture() {
    return kind == eKind.departure;
  }
}
