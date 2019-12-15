package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.jAtcSim.newLib.shared.Callsign;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class MovementTemplate implements IMovementTemplate {
  private Callsign callsign;
  private ETimeStamp time;
  private String airplaneTypeName;
  private int delayInMinutes;
}
