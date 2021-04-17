package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public interface IScheduledMovement {
  AirplaneType getAirplaneType();

  EDayTimeStamp getScheduledTime();

  EDayTimeStamp getScheduledTimeWithDelay();

  Callsign getCallsign();

  int getDelayInMinutes();

  DepartureArrival getDirection();
}
