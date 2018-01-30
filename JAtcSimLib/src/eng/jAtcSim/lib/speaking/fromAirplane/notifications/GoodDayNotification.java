package eng.jAtcSim.lib.speaking.fromAirplane.notifications;

import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.airplanes.Callsign;
import eng.jAtcSim.lib.speaking.fromAirplane.IAirplaneNotification;

public class GoodDayNotification implements IAirplaneNotification {
  // TODO here callsign should not be, as it is known from the sender.
  private final Callsign callsign;
  private final String altitudeInfoText;

  public GoodDayNotification(Callsign callsign, String altitudeInfoText) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");
    if (altitudeInfoText == null)
      throw new IllegalArgumentException("Argument \"altitudeInfoText\" cannot be null.");

    this.callsign = callsign;
    this.altitudeInfoText = altitudeInfoText;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public String getAltitudeInfoText() {
    return altitudeInfoText;
  }

  @Override
  public String toString(){
    String ret = "Welcome greeting from plane " + this.callsign.toString() + " {notification}";

    return ret;
  }
}
