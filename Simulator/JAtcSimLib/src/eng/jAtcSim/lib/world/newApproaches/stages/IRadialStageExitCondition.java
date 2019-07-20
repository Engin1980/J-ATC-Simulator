package eng.jAtcSim.lib.world.newApproaches.stages;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRO;

public interface IRadialStageExitCondition {
  boolean isTrue(IAirplaneRO plane);
}
