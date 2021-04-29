package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.exceptions.UnexpectedValueException;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.enums.DepartureArrival;
import eng.jAtcSim.newLib.stats.AnalysedPlanes;
import eng.jAtcSim.newLib.stats.FinishedPlaneStats;
import eng.jAtcSim.newLib.stats.contextLocal.Context;

public class StatsProvider {

  private final int blockLength;
  private final IList<IStatsBlock> blocks = new EList<>();
  private final IList<MoodResult> moodResults = new EList<>();

  public StatsProvider(int blockLength) {
    this.blockLength = blockLength;
  }

  public void elapseSecond(AnalysedPlanes analysedPlanes) {
    migrateLiveBlockToSnapshotIfRequired();
    LiveStatsBlock b = getLiveStatsBlock();
    b.elapseSecond(analysedPlanes);
  }

  public void init() {
    blocks.add(new LiveStatsBlock(Context.getShared().getNow().toStamp()));
  }

  private void migrateLiveBlockToSnapshotIfRequired() {
    EAssert.isTrue(blocks.getLast() instanceof LiveStatsBlock);
    if (blocks.getLast().getCoveredSecondsCount() == blockLength) {
      LiveStatsBlock b = getLiveStatsBlock();
      SnapshotStatsBlock s = new SnapshotStatsBlock(b);
      blocks.set(blocks.size() - 1, s);
      blocks.add(new LiveStatsBlock(Context.getShared().getNow().toStamp()));
    }
  }

  private LiveStatsBlock getLiveStatsBlock() {
    return (LiveStatsBlock) blocks.getLast();
  }

  public void registerFinishedPlane(FinishedPlaneStats finishedPlaneStats) {
    getLiveStatsBlock().getFinishedArrivalDelays().addValue(finishedPlaneStats.getDelayDifference());
  }

  public void registerFinishedPlane(FinishedPlaneStats finishedPlaneStats) {
    MoodResult mr = finishedPlaneStats.getMoodResult();
    this.moodResults.add(mr);

    LiveStatsBlock b = getLiveStatsBlock();
    if (finishedPlaneStats.isArrival()){
      b.getFinishedArrivalsMoodValues().addValue(mr);
      if (!finishedPlaneStats.isEmergency())
        b.getFinishedArrivalDelays().addValue(finishedPlaneStats.getDelayDifference());
    } else {
      b.getFinishedDeparturesMoodValues().addValue(mr);
      if (!finishedPlaneStats.isEmergency())
        b.getFinishedDepartureDelays().addValue(finishedPlaneStats.getDelayDifference());
    }
  }

  public void registerHoldingPointDelay(int delay) {
    getLiveStatsBlock().getHoldingPointDelays().addValue(delay);
  }

  public void registerNewArrivalOrDeparture(DepartureArrival departureArrival) {
    switch (departureArrival){
      case departure:
        getLiveStatsBlock().getDeparturesCount().addValue(1);
      case arrival:
        getLiveStatsBlock().getArrivalsCount().addValue(1);
      default:
        throw new UnexpectedValueException(departureArrival);
    }
  }
}
