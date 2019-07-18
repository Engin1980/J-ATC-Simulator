package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class NewApproachInfo {
  private final ApproachEntry entry;
  private final IList<IApproachStage> stages;
  private final SpeechList<IAtcCommand> gaCommands;
  private final ActiveRunwayThreshold runwayThreshold;
  private final Approach.ApproachType type;


  public NewApproachInfo(ApproachEntry entry, Approach approach) {
    assert entry != null;
    assert approach != null;
    assert approach.getEntries().contains(entry) : "Entry must be related to the approach";

    this.entry = entry;
    this.stages = new EList<>(approach.getStages());
    this.gaCommands = approach.getGaCommands().clone();
    this.runwayThreshold = approach.getParent();
    this.type = approach.getType();
  }

  public ApproachEntry getEntry() {
    return entry;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return gaCommands;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  public Approach.ApproachType getType() {
    return type;
  }
}
