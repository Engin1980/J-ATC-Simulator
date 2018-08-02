package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;

public class GoodDayNotification implements IAirplaneNotification {
  // TODO here callsign should not be, as it is known from the sender.
  private final Callsign callsign;
  private final double altitude;
  private final boolean emergency;
  private final boolean repeated;

  public GoodDayNotification(Callsign callsign, double altitude, boolean emergecny, boolean repeated) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");

    this.callsign = callsign;
    this.altitude = altitude;
    this.emergency = emergecny;
    this.repeated = repeated;
  }

  public boolean isEmergency() {
    return emergency;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public double getAltitude() {
    return altitude;
  }

  public boolean isRepeated() {
    return repeated;
  }

  @Override
  public String toString(){
    String ret = "Welcome greeting from plane " + this.callsign.toString() + " {notification}";
    if (emergency)
      ret += "{emergency}";
    if (repeated)
      ret += "{repeated}";

    return ret;
  }
}
