package eng.jAtcSim.newLib.airplanes.templates;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.annotations.XConstructor;
import exml.annotations.XIgnored;

import java.util.Comparator;

public abstract class AirplaneTemplate {
  private final Callsign callsign;
  private final AirplaneType airplaneType;
  protected final EntryExitPoint entryExitPoint;
  private final EDayTimeStamp expectedExitTime;
  private final EDayTimeStamp entryTime;
  private final int entryDelay;
  @XIgnored private EDayTimeStamp entryTimeWithEntryDelay = null;

  public static class ByEntryTimeWithEntryDelayComparer implements Comparator<AirplaneTemplate>{

    @Override
    public int compare(AirplaneTemplate a, AirplaneTemplate b) {
      return Integer.compare(a.getEntryTimeWithEntryDelay().getValue(), b.getEntryTimeWithEntryDelay().getValue());
    }
  }

  @XConstructor
  protected AirplaneTemplate() {
    this.callsign = null;
    this.airplaneType = null;
    this.entryExitPoint = null;
    this.expectedExitTime = null;
    this.entryTime = null;
    this.entryDelay = 0;

    PostContracts.register(this, () -> callsign != null);
    PostContracts.register(this, () -> airplaneType != null);
    PostContracts.register(this, () -> entryExitPoint != null);
    PostContracts.register(this, () -> expectedExitTime != null);
    PostContracts.register(this, () -> entryTime != null);
  }

  public AirplaneTemplate(Callsign callsign, AirplaneType airplaneType, EntryExitPoint entryExitPoint,
                          EDayTimeStamp expectedExitTime, EDayTimeStamp entryTime, int entryDelay) {

    EAssert.Argument.isNotNull(callsign, "callsign");
    EAssert.Argument.isNotNull(airplaneType, "airplaneType");
    EAssert.Argument.isNotNull(entryExitPoint, "entryExitPoint");
    EAssert.Argument.isNotNull(expectedExitTime, "expectedExitTime");
    EAssert.Argument.isNotNull(entryTime, "entryTime");
    EAssert.Argument.isNotNull(entryDelay, "entryDelay");
    EAssert.Argument.isTrue(entryTime.isBefore(expectedExitTime));

    this.callsign = callsign;
    this.airplaneType = airplaneType;
    this.entryExitPoint = entryExitPoint;
    this.expectedExitTime = expectedExitTime;
    this.entryTime = entryTime;
    this.entryDelay = entryDelay;
  }

  public AirplaneType getAirplaneType() {
    return airplaneType;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getEntryDelay() {
    return entryDelay;
  }

  public EDayTimeStamp getEntryTime() {
    return entryTime;
  }

  public EDayTimeStamp getEntryTimeWithEntryDelay() {
    if (entryTimeWithEntryDelay == null)
      entryTimeWithEntryDelay = this.entryTime.addMinutes(this.entryDelay);
    return entryTimeWithEntryDelay;
  }

  public EDayTimeStamp getExpectedExitTime() {
    return expectedExitTime;
  }

  @Override
  public String toString() {
    return "{" + this.getClass().getSimpleName() + "} " + this.getCallsign();
  }
}
