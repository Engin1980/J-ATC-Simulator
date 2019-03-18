package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.airplanes.pilots.approachStages.IApproachStage;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;

public class Approach {

  public enum ApproachType {
    ils_I,
    ils_II,
    ils_III,
    ndb,
    vor,
    gnss,
    visual
  }

  private ApproachEntryLocation approachEntries;
  private final SpeechList<IAtcCommand> gaCommands;
  private final PlaneCategoryDefinitions planeCategories;
  private final IList<IafRoute> iafRoutes;
  private final ActiveRunwayThreshold parent;
  private final ApproachType type;
  private final ApproachEntryLocation entryLocation;
  private final IList<IApproachStage> stages;

  public Approach(ApproachType type, PlaneCategoryDefinitions planeCategories, SpeechList<IAtcCommand> gaCommands,
                  ApproachEntryLocation entryLocation, IList<IApproachStage> stages,
                  IList<IafRoute> iafRoutes, ActiveRunwayThreshold parent) {
    this.planeCategories = planeCategories;
    this.gaCommands = gaCommands;
    this.iafRoutes = iafRoutes;
    this.parent = parent;
    this.type = type;
    this.entryLocation = entryLocation;
    this.stages = stages;
  }

  public ApproachEntryLocation getApproachEntries() {
    return approachEntries;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return gaCommands;
  }

  public PlaneCategoryDefinitions getPlaneCategories() {
    return planeCategories;
  }

  public IReadOnlyList<IafRoute> getIafRoutes() {
    return iafRoutes;
  }

  public ActiveRunwayThreshold getParent() {
    return parent;
  }

  public ApproachType getType() {
    return type;
  }

  public ApproachEntryLocation getEntryLocation() {
    return entryLocation;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }
}
