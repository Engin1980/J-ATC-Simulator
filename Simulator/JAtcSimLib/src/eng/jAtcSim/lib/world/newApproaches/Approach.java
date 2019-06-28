package eng.jAtcSim.lib.world.newApproaches;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.eSystem.exceptions.EApplicationException;
import eng.jAtcSim.lib.world.newApproaches.stages.IApproachStage;
import eng.jAtcSim.lib.global.PlaneCategoryDefinitions;
import eng.jAtcSim.lib.speaking.SpeechList;
import eng.jAtcSim.lib.speaking.fromAtc.IAtcCommand;
import eng.jAtcSim.lib.world.ActiveRunwayThreshold;
import eng.jAtcSim.lib.world.newApproaches.checkPoints.ApproachCheckPoint;
import eng.jAtcSim.lib.world.newApproaches.entryLocations.ApproachEntryLocation;

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

  private IList<ApproachEntry> approachEntries;
  private final IList<IApproachStage> stages;
  private final IList<ApproachCheckPoint> checkPoints;
  private final SpeechList<IAtcCommand> gaCommands;
  private final PlaneCategoryDefinitions planeCategories;
  private final ActiveRunwayThreshold parent;
  private final ApproachType type;
  private final ApproachEntryLocation entryLocation;


  public Approach(ApproachType type, PlaneCategoryDefinitions planeCategories, SpeechList<IAtcCommand> gaCommands,
                  ApproachEntryLocation entryLocation, IList<IApproachStage> stages,
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

  public ApproachEntryLocation getEntryLocation() {
    return entryLocation;
  }

  public IReadOnlyList<IApproachStage> getStages() {
    return stages;
  }
}
