package eng.jAtcSim.newLib.gameSim.simulation;

import eng.jAtcSim.newLib.airplaneType.AirplaneType;
import eng.jAtcSim.newLib.airplanes.templates.AirplaneTemplate;
import eng.jAtcSim.newLib.airplanes.templates.ArrivalAirplaneTemplate;
import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import exml.annotations.XIgnored;

public class ScheduledMovement implements IScheduledMovement {

  private final AirplaneType airplaneType;
  @XIgnored private EDayTimeStamp scheduledTimeWithDelay = null;
  private final EDayTimeStamp scheduledTime;
  private final Callsign callsign;
  private final int delayInMinutes;
  private final DepartureArrival departureArrival;

  public ScheduledMovement(AirplaneTemplate q) {
    this.airplaneType = q.getAirplaneType();
    this.callsign = q.getCallsign();
    this.scheduledTime = q.getEntryTime();
    this.delayInMinutes = q.getEntryDelay();
    this.departureArrival = (q instanceof ArrivalAirplaneTemplate) ? DepartureArrival.arrival : DepartureArrival.departure;
  }

  @Override
  public AirplaneType getAirplaneType() {
    return airplaneType;
  }

  @Override
  public Callsign getCallsign() {
    return callsign;
  }

  @Override
  public int getDelayInMinutes() {
    return delayInMinutes;
  }

  @Override
  public DepartureArrival getDirection() {
    return departureArrival;
  }

  @Override
  public EDayTimeStamp getScheduledTime() {
    return scheduledTime;
  }

  @Override
  public EDayTimeStamp getScheduledTimeWithDelay() {
    if (scheduledTimeWithDelay == null) {
      scheduledTimeWithDelay = scheduledTime.addMinutes(delayInMinutes);
    }
    return scheduledTimeWithDelay;
  }
}
