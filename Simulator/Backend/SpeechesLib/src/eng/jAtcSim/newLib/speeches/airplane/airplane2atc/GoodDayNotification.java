package eng.jAtcSim.newLib.speeches.airplane.airplane2atc;

import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.speeches.airplane.IFromPlaneSpeech;

public class GoodDayNotification implements IFromPlaneSpeech {
  private final Callsign callsign;
  private final int altitude;
  private final int targetAltitude;
  private final boolean emergency;
  private final boolean repeated;

  public GoodDayNotification(Callsign callsign, int altitude, int targetAltitude, boolean emergecny, boolean repeated) {
    if (callsign == null)
      throw new IllegalArgumentException("Argument \"callsign\" cannot be null.");

    this.callsign = callsign;
    this.altitude = altitude;
    this.targetAltitude = targetAltitude;
    this.emergency = emergecny;
    this.repeated = repeated;
  }

  public int getAltitude() {
    return altitude;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getTargetAltitude() {
    return targetAltitude;
  }

  public boolean isEmergency() {
    return emergency;
  }

  public boolean isRepeated() {
    return repeated;
  }

  @Override
  public String toString() {
    String ret = "Welcome greeting from plane " + this.callsign.toString() + " {notification}";
    if (emergency)
      ret += "{emergency}";
    if (repeated)
      ret += "{repeated}";

    return ret;
  }
}
