package eng.jAtcSim.lib.airplanes.pilots.approachStages;

import eng.jAtcSim.lib.airplanes.pilots.behaviors.IPilot4Behavior;

public interface IApproachStage {
  void initStage(IPilot4Behavior pilot);
  void flyStage(IPilot4Behavior pilot);
  void disposeStage(IPilot4Behavior pilot);
  boolean isFinishedStage(IPilot4Behavior pilot);
}
