package eng.jAtcSim.newLib.speaking.fromAirplane.notifications;

import eng.jAtcSim.newLib.airplanes.Callsign;
import eng.jAtcSim.newLib.speaking.fromAirplane.IAirplaneNotification;

public class GoodDayNotification implements IAirplaneNotification {
  private final Callsign callsign;
  private final double altitude;
  private final double targetAltitude;
  private final boolean emergency;
  private final boolean repeated;

  public GoodDayNotification(Callsign callsign, double altitude, double targetAltitude, boolean emergecny, boolean repeated) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");

    this.callsign = callsign;
    this.altitude = altitude;
    this.targetAltitude = targetAltitude;
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

  public double getTargetAltitude() {
    return targetAltitude;
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
