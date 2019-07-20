package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRead;

public interface IRadialStageExitCondition {
  boolean isTrue(IAirplaneRead plane);
}
