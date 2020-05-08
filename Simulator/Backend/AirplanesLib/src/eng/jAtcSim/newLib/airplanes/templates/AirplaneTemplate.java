package eng.jAtcSim.newLib.airplanes.templates;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public abstract class AirplaneTemplate {
  private final Callsign callsign;
  private final AirplaneType airplaneType;
  protected final EntryExitPoint entryExitPoint;
  private final EDayTimeStamp expectedExitTime;
  private final EDayTimeStamp entryTime;
  private final int entryDelay;

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

  public EDayTimeStamp getEntryTime() {
    return entryTime;
  }

  public EDayTimeStamp getExpectedExitTime() {
    return expectedExitTime;
  }

  public int getEntryDelay() {
    return entryDelay;
  }
}