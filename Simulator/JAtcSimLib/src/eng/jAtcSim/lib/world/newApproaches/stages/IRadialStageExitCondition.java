package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotRO;
import eng.jAtcSim.lib.airplanes.pilots.interfaces.forPilot.IPilotWriteSimple;

public interface IRadialStageExitCondition {
  boolean isTrue(IPilotRO pilot);
}
