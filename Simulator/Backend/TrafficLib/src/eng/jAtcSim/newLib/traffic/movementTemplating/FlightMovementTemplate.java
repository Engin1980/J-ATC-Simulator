package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class FlightMovementTemplate extends MovementTemplate {
  private Callsign callsign;
  private String airplaneTypeName;

  public FlightMovementTemplate(Callsign callsign, String airplaneTypeName,
                                eKind kind, ETimeStamp time,
                                EntryExitInfo entryExitInfo) {
    super(kind, time, entryExitInfo);
    EAssert.isNotNull(callsign);
    EAssert.isNonemptyString(airplaneTypeName);
    this.callsign = callsign;
    this.airplaneTypeName = airplaneTypeName;
  }

  public String getAirplaneTypeName() {
    return airplaneTypeName;
  }

  public Callsign getCallsign() {
    return callsign;
  }
}
