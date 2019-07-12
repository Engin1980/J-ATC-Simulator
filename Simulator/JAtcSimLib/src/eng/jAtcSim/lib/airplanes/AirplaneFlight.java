package eng.jAtcSim.lib.airplanes;

import eng.jAtcSim.lib.global.ETime;

public class AirplaneFlight {
  private final Callsign callsign;
  private final int delayInitialMinutes;
  private final ETime delayExpectedTime;
  private boolean departure;

  public AirplaneFlight(Callsign callsign, int delayInitialMinutes, ETime delayExpectedTime, boolean departure) {
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
}
