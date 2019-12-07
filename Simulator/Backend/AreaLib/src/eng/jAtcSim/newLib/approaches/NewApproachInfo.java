package eng.jAtcSim.newLib.approaches;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.approaches.stages.IApproachStage;
import eng.jAtcSim.newLib.approaches.stages.RouteStage;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.speeches.IAtcCommand;

public class NewApproachInfo {
  private final IList<IApproachStage> stages;
  private final IList<IAtcCommand> gaCommands;
  private final ActiveRunwayThreshold runwayThreshold;
  private final Approach.ApproachType type;


  public NewApproachInfo(ApproachEntry entry, Approach approach) {
    assert entry != null;
    assert approach != null;
    assert approach.getEntries().contains(entry) : "Entry must be related to the approach";

    this.stages = new EList<>(approach.getStages());
    this.stages.insert(0, new RouteStage(entry.getIafRoute().getRouteCommands()));
    this.gaCommands = new EList<>(approach.getGaRoute().getRouteCommands());
    this.runwayThreshold = approach.getParent();
    this.type = approach.getType();
  }

  public IReadOnlyList<IAtcCommand> getGaCommands() {
    return gaCommands;
  }

  public ActiveRunwayThreshold getRunwayThreshold() {
    return runwayThreshold;
  }

  public IList<IApproachStage> getStages() {
    return stages;
  }

  public Approach.ApproachType getType() {
    return type;
  }
}
