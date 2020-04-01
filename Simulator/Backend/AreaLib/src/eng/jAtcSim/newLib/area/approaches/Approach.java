package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;
import eng.jAtcSim.newLib.speeches.ICommand;

public class Approach {
  public static Approach create(ApproachType type, IList<ApproachEntry> entries, IList<ICommand> beforeStagesCommands, IList<ApproachStage> stages, GaRoute gaRoute) {
    return new Approach(type, entries, beforeStagesCommands, stages, gaRoute);
  }
  private final ApproachType type;
  private final IList<ApproachEntry> entries;
  private final IList<ICommand> beforeStagesCommands;
  private final IList<ApproachStage> stages;
  private final GaRoute gaRoute;

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

  public ApproachType getType() {
    return type;
  }

  public IReadOnlyList<ApproachEntry> getEntries() {
    return entries;
  }

  public IReadOnlyList<ICommand> getBeforeStagesCommands() {
    return beforeStagesCommands;
  }

  public IReadOnlyList<ApproachStage> getStages() {
    return stages;
  }

  public GaRoute getGaRoute() {
    return gaRoute;
  }
}
