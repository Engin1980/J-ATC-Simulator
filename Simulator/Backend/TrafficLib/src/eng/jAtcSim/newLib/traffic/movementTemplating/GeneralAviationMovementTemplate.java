package eng.jAtcSim.newLib.traffic.movementTemplating;

import eng.eSystem.collections.*;
import eng.jAtcSim.newLib.shared.time.ETimeStamp;

public class GeneralAviationMovementTemplate extends MovementTemplate {
  public GeneralAviationMovementTemplate(eKind kind, ETimeStamp time, int delayInMinutes, EntryExitInfo entryExitInfo) {
    super(kind, time, delayInMinutes, entryExitInfo);
  }
}
