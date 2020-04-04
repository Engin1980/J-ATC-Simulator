package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETime;

public class AirplaneFlightModule {
  private final Callsign callsign;
  private final int delayInitialMinutes;
  private final ETime delayExpectedTime;
  private boolean departure;
  private Integer finalDelayMinutes = null;

  public AirplaneFlightModule(Callsign callsign, int delayInitialMinutes, ETime delayExpectedTime, boolean departure) {
    this.callsign = callsign;
    this.delayInitialMinutes = delayInitialMinutes;
    this.delayExpectedTime = delayExpectedTime;
    this.departure = departure;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getDelayInitialMinutes() {
    return delayInitialMinutes;
  }

  public ETime getDelayExpectedTime() {
    return delayExpectedTime;
  }

  public boolean isDeparture() {
    return departure;
  }

  public boolean isArrival() {
    return !isDeparture();
  }

  public void raiseEmergency() {
    this.departure = false;
  }

  public Integer getFinalDelayMinutes() {
    return finalDelayMinutes;
  }

  public void divert() {
    this.departure = true;
  }

  public int getDelayDifference() {
    return finalDelayMinutes;
  }
}
