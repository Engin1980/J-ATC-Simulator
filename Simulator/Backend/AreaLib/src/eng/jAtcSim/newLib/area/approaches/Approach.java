package eng.jAtcSim.newLib.area.approaches;

import eng.eSystem.collections.*;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.area.routes.GaRoute;
import eng.jAtcSim.newLib.shared.enums.ApproachType;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class Approach {
  private final ApproachType type;
  private final IList<ApproachEntry> entries;
  private final IList<ApproachStage> stages;
  private final GaRoute gaRoute;

  public Approach(ApproachType type, IList<ApproachEntry> entries, IList<ApproachStage> stages, GaRoute gaRoute) {
    EAssert.Argument.isNotNull(entries, "entries");
    EAssert.Argument.isTrue(entries.size() > 0, "There must be at least one approach entry.");
    EAssert.Argument.isNotNull(stages, "stages");
    EAssert.Argument.isTrue(stages.size() > 0, "There must be at least one approach stage.");
    EAssert.Argument.isNotNull(gaRoute, "gaRoute");
    this.type = type;
    this.entries = entries;
    this.stages = stages;
    this.gaRoute = gaRoute;
  }
}
