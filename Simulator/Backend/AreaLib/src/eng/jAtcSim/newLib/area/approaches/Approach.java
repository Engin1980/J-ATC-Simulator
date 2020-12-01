package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.geo.Headings;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.ActiveRunwayThreshold;
import eng.jAtcSim.newLib.area.Parentable;
import eng.jAtcSim.newLib.area.approaches.behaviors.FlyRadialBehavior;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.airplane.ICommand;

public class Approach extends Parentable<ActiveRunwayThreshold> {

  public static Approach create(ApproachType type, IList<ApproachEntry> entries, IList<ICommand> beforeStagesCommands, IList<ApproachStage> stages, GaRoute gaRoute) {
    return new Approach(type, entries, beforeStagesCommands, stages, gaRoute);
  }

  private final ApproachType type;
  private final IList<ApproachEntry> entries;
  private final IList<ICommand> beforeStagesCommands;
  private final IList<ApproachStage> stages;
  private final GaRoute gaRoute;
  private Integer calculatedGeographicalRadial;

  public Approach(ApproachType type, IList<ApproachEntry> entries, IList<ICommand> beforeStagesCommands, IList<ApproachStage> stages, GaRoute gaRoute) {
    EAssert.Argument.isNotNull(entries, "entries");
    EAssert.Argument.isTrue(entries.size() > 0, "There must be at least one approach entry.");
    EAssert.Argument.isNotNull(stages, "stages");
    EAssert.Argument.isTrue(stages.size() > 0, "There must be at least one approach stage.");
    EAssert.Argument.isNotNull(gaRoute, "gaRoute");
    this.type = type;
    this.entries = entries;
    this.beforeStagesCommands = beforeStagesCommands;
    this.stages = stages;
    this.gaRoute = gaRoute;
  }

  public IReadOnlyList<ICommand> getBeforeStagesCommands() {
    return beforeStagesCommands;
  }

  public IReadOnlyList<ApproachEntry> getEntries() {
    return entries;
  }

  public GaRoute getGaRoute() {
    return gaRoute;
  }

  public int getGeographicalRadial() {
    if (calculatedGeographicalRadial == null)
      this.calculatedGeographicalRadial = calculateGeographicalRadial();
    return calculatedGeographicalRadial;
  }

  public IReadOnlyList<ApproachStage> getStages() {
    return stages;
  }

  public ApproachType getType() {
    return type;
  }

  private int calculateGeographicalRadial() {
    ApproachStage stage = getStages().tryGetLast(q -> q.getBehavior() instanceof FlyRadialBehavior);
    FlyRadialBehavior frb = (FlyRadialBehavior) stage.getBehavior();
    int radial = frb.getInboundRadial();
    int ret = (int) Math.round(
        Headings.add(radial,
            this.getParent().getParent().getParent().getDeclination()));
    return ret;
  }
}
