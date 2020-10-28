package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.eXml.XElement;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.jAtcSim.newLib.shared.xml.SharedXmlUtils;
import eng.jAtcSimLib.xmlUtils.XmlSaveUtils;

public abstract class MovementTemplate {
  public enum eKind {
    arrival,
    departure
  }

  private final eKind kind;
  private final ETimeStamp appearanceTime;
  private final EntryExitInfo entryExitInfo;

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

  public void save(XElement target) {
    XmlSaveUtils.Field.storeField(target, this, "kind");
    XmlSaveUtils.Field.storeField(target, this, "appearanceTime",
            SharedXmlUtils.iTimeFormatter);
    XmlSaveUtils.Field.storeField(target, this, "entryExitInfo",
            (XElement e, EntryExitInfo q) -> q.save(e));
  }
}
