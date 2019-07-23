package eng.jAtcSim.lib.world.approaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.approaches.entryLocations.IApproachEntryLocation;
import eng.jAtcSim.lib.world.approaches.stages.IApproachStage;

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

  private final IList<ApproachEntry> entries;
  private final IList<IApproachStage> stages;
  private final SpeechList<IAtcCommand> gaCommands;
  private final PlaneCategoryDefinitions planeCategories;
  private final ActiveRunwayThreshold parent;
  private final ApproachType type;


  public Approach(ApproachType type, PlaneCategoryDefinitions planeCategories, SpeechList<IAtcCommand> gaCommands,
                  IApproachEntryLocation entryLocation, IList<IApproachStage> stages,
                  IList<IafRoute> iafRoutes, ActiveRunwayThreshold parent) {
    throw new EApplicationException("Must be implemented.");
//    this.planeCategories = planeCategories;
//    this.gaCommands = gaCommands;
//    this.iafRoutes = iafRoutes;
//    this.parent = parent;
//    this.type = type;
//    this.entryLocation = entryLocation;
//    this.stages = stages;
  }

  public IReadOnlyList<ApproachEntry> getEntries() {
    return entries;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }

  public SpeechList<IAtcCommand> getGaCommands() {
    return gaCommands;
  }

  public PlaneCategoryDefinitions getPlaneCategories() {
    return planeCategories;
  }

  public ActiveRunwayThreshold getParent() {
    return parent;
  }

  public ApproachType getType() {
    return type;
  }
}
