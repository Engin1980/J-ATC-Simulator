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

  private final ApproachType type;
  private final IList<ApproachEntry> entries;
  private final IList<ICommand> beforeStagesCommands;
  private final IList<ApproachStage> stages;
  private final GaRoute gaRoute;
  private final int initialAltitude;
  private Integer calculatedGeographicalRadial;

  public Approach(ApproachType type, int initialAltitude, IList<ApproachEntry> entries, IList<ICommand> beforeStagesCommands, IList<ApproachStage> stages, GaRoute gaRoute) {
    EAssert.Argument.isNotNull(entries, "entries");
    EAssert.Argument.isTrue(entries.size() > 0, "There must be at least one approach entry.");
    EAssert.Argument.isNotNull(stages, "stages");
    EAssert.Argument.isTrue(stages.size() > 0, "There must be at least one approach stage.");
    EAssert.Argument.isNotNull(gaRoute, "gaRoute");
    this.type = type;
    this.initialAltitude = initialAltitude;
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

  public int getInitialAltitude() {
    return initialAltitude;
  }

  public IReadOnlyList<ApproachStage> getStages() {
    return stages;
  }

  public ApproachType getType() {
    return type;
  }

  private int calculateGeographicalRadial() {
    ApproachStage stage = getStages().getLast(q -> q.getBehavior() instanceof FlyRadialBehavior);
    FlyRadialBehavior frb = (FlyRadialBehavior) stage.getBehavior();
    int ret = (int) Math.round(frb.getInboundRadialWithDeclination());
    return ret;
  }
}
