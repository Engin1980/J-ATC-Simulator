package eng.jAtcSim.lib.world.approaches.stages;

import eng.jAtcSim.lib.airplanes.interfaces.IAirplaneRO;

public interface IRadialStageExitCondition {
  boolean isTrue(IAirplaneRO plane);
}
