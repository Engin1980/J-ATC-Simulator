package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;

public class GoodDayNotification implements IAirplaneNotification {
  // TODO here callsign should not be, as it is known from the sender.
  private final Callsign callsign;
  private final double altitude;

  public GoodDayNotification(Callsign callsign, double altitude) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");

    this.callsign = callsign;
    this.altitude = altitude;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public double getAltitude() {
    return altitude;
  }

  @Override
  public String toString(){
    String ret = "Welcome greeting from plane " + this.callsign.toString() + " {notification}";

    return ret;
  }
}
