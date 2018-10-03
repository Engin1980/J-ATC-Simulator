package eng.jAtcSim.lib.stats.write;

import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.write.specific.*;

public class WriteSet {
  public ETime fromTime;
  public SecondStats secondStats = new SecondStats();
  public PlanesStats planes = new PlanesStats();
  public MoodStatsItem planesMood = new MoodStatsItem();
  public HoldingPointStats holdingPoint = new HoldingPointStats();
  public ErrorsStats errors = new ErrorsStats();

  public WriteSet(ETime fromTime) {
    this.fromTime = fromTime;
  }
}
