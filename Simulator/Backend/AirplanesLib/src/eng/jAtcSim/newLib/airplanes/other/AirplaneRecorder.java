package eng.jAtcSim.newLib.airplanes.other;

import eng.jAtcSim.newLib.shared.Callsign;

public abstract class AirplaneRecorder {
  protected final Callsign callsign;

  public AirplaneRecorder(Callsign callsign) {
    this.callsign = callsign;
  }
}
