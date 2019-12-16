package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public abstract class MovementTemplate implements IMovementTemplate {
  private Callsign callsign;
  private ETimeStamp time;
  private String airplaneTypeName;
  private int delayInMinutes;

  public MovementTemplate(Callsign callsign, String airplaneTypeName, ETimeStamp time, int delayInMinutes) {
    this.callsign = callsign;
    this.time = time;
    this.airplaneTypeName = airplaneTypeName;
    this.delayInMinutes = delayInMinutes;
  }

  public Callsign getCallsign() {
    return callsign;
  }

  public ETimeStamp getTime() {
    return time;
  }

  public String getAirplaneTypeName() {
    return airplaneTypeName;
  }

  public int getDelayInMinutes() {
    return delayInMinutes;
  }
}
