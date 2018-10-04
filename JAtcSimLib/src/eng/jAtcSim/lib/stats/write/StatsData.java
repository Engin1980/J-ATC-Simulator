package eng.jAtcSim.lib.stats.write;

import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.write.specific.*;

public class StatsData {
  public ETime fromTime;
  public ETime toTime;
  public SecondStats secondStats = new SecondStats();
  public PlanesStats planes = new PlanesStats();
  public MoodStatsItem planesMood = new MoodStatsItem();
  public HoldingPointStats holdingPoint = new HoldingPointStats();
  public ErrorsStats errors = new ErrorsStats();

  public StatsData(ETime fromTime) {
    this.fromTime = fromTime;
    this.toTime = null;
  }

  public boolean isLive(){
    return toTime == null;
  }
}
