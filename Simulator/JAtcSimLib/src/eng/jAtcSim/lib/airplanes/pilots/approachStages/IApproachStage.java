package eng.jAtcSim.lib.airplanes.pilots.approachStages;

public interface IApproachStage {
  void init(IPilot4ApproachStage pilot);
  void fly(IPilot4ApproachStage pilot);
  boolean isFinished
}
