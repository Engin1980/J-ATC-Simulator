package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.PostContracts;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;
import eng.newXmlUtils.annotations.XmlConstructor;

public class FlightMovementTemplate extends MovementTemplate {

  private final Callsign callsign;
  private final String airplaneTypeName;

  @XmlConstructor
  private FlightMovementTemplate(){
    super(eKind.arrival, null, null);
    this.callsign = null;
    this.airplaneTypeName = null;
    PostContracts.register(this,
            () -> callsign != null &&airplaneTypeName != null);
  }

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
