package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.newStats.blocks.IStatsBlock;
import eng.jAtcSim.newLib.newStats.blocks.LiveStatsBlock;
import eng.jAtcSim.newLib.newStats.blocks.SnapshotStatsBlock;
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

  public void registerFinishedPlane(FinishedPlaneStats finishedPlaneStats) {
    MoodResult mr = finishedPlaneStats.getMoodResult();
    this.moodResults.add(mr);

    LiveStatsBlock b = getLiveStatsBlock();
    if (finishedPlaneStats.isArrival()) {
      b.getFinishedArrivalsMoodValues().addValue(mr.getPoints());
      if (!finishedPlaneStats.isEmergency())
        b.getFinishedArrivalDelays().addValue(finishedPlaneStats.getDelayDifference());
    } else {
      b.getFinishedDeparturesMoodValues().addValue(mr.getPoints());
      if (!finishedPlaneStats.isEmergency())
        b.getHoldingPointDelays().addValue(finishedPlaneStats.getDelayDifference());
    }
  }

  public void registerLandedPlane() {
    getLiveStatsBlock().getArrivalsCount().addValue(1);
  }

  public void registerTakenOffPlane(int holdingPointSeconds) {
    getLiveStatsBlock().getHoldingPointDelays().addValue(holdingPointSeconds);
    getLiveStatsBlock().getDeparturesCount().addValue(1);
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
}
