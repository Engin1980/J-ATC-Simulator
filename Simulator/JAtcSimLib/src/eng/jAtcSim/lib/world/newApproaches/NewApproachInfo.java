package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.IApproachStage;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class NewApproachInfo {
  private final Approach approach;
  private final IList<IApproachStage> stages;
  private final IafRoute iafRoute;

  public NewApproachInfo(Approach approach) {
    this(approach, null);
  }

  public NewApproachInfo(Approach approach, IafRoute iafRoute) {
    this.approach =approach;
    this.iafRoute = iafRoute;
    this.stages = new EList<>(approach.getStages());
  }

  public Approach getApproach() {
    return approach;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public IafRoute getIafRoute() {
    return iafRoute;
  }

  public ActiveRunwayThreshold getThreshold() {
    return this.approach.getParent();
  }
}
