package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;
import eng.jAtcSim.lib.airplanes.pilots.behaviors.NewApproachBehavior;

public interface IApproachStage {
  void initStage(IPilot4Behavior behavior);
  void flyStage(IPilot4Behavior behavior);
  void disposeStage(IPilot4Behavior behavior);
  boolean isFinishedStage(IPilot4Behavior behavior);
}
