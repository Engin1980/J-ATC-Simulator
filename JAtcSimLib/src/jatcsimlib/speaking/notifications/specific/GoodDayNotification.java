package jatcsimlib.speaking.notifications.specific;

import jatcsimlib.airplanes.Callsign;
import jatcsimlib.speaking.notifications.Notification;

public class GoodDayNotification extends Notification {
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
}
