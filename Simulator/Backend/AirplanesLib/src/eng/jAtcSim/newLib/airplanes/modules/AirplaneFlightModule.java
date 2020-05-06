package eng.jAtcSim.newLib.airplanes.modules;


import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;

public class AirplaneFlightModule {
  private final Callsign callsign;
  private final int entryDelay;
  private final EDayTimeStamp expectedExitTime;
  private EDayTimeStamp exitTime = null;
  private boolean departure;

  public AirplaneFlightModule(Callsign callsign, int entryDelay, EDayTimeStamp expectedExitTime, boolean departure) {
    this.callsign = callsign;
    this.entryDelay = entryDelay;
    this.expectedExitTime = expectedExitTime;
    this.departure = departure;
  }

  public void divert() {
    this.departure = true;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public int getEntryDelay() {
    return entryDelay;
  }

  public EDayTimeStamp getExpectedExitTime() {
    return expectedExitTime;
  }

  public boolean isArrival() {
    return !isDeparture();
  }

  public boolean isDeparture() {
    return departure;
  }

  public void raiseEmergency() {
    this.departure = false;
  }
}
