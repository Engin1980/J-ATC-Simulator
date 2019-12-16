package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class ArrivalMovementTemplate extends MovementTemplate implements IArrivalMovement {
  private ArrivalEntryInfo arrivalEntryInfo;

  public ArrivalMovementTemplate(Callsign callsign, String airplaneTypeName, ETimeStamp time, int delayInMinutes, ArrivalEntryInfo arrivalEntryInfo) {*
    super(callsign, airplaneTypeName, time, delayInMinutes);

    if (arrivalEntryInfo == null) {
        throw new IllegalArgumentException("Value of {arrivalEntryInfo} cannot not be null.");
    }
    this.arrivalEntryInfo = arrivalEntryInfo;
  }

  public ArrivalEntryInfo getArrivalEntryInfo() {
    return arrivalEntryInfo;
  }
}
