package eng.jAtcSim.newLib.airplanes.templates;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.area.EntryExitPoint;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public final class DepartureAirplaneTemplate extends AirplaneTemplate {
  public DepartureAirplaneTemplate(Callsign callsign, AirplaneType airplaneType, EntryExitPoint entryExitPoint,
                                   EDayTimeStamp entryTime, int entryDelay, EDayTimeStamp expectedExitTime) {
    super(callsign, airplaneType, entryExitPoint, expectedExitTime, entryTime, entryDelay);
  }

  public EntryExitPoint getExitPoint(){
    return super.entryExitPoint;
  }
}
