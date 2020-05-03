package eng.jAtcSim.newLib.stats;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class StatsAcc {

  private static Producer<IOverallStatsWriter> overallStatsWriterProducer = null;
  private static Producer<IRecentStatsWriter> recentStatsWriterProducer = null;

  public static void setOverallStatsWriterProducer(Producer<IOverallStatsWriter> overallStatsWriterProducer) {
    StatsAcc.overallStatsWriterProducer = overallStatsWriterProducer;
  }

  public static void setRecentStatsWriterProducer(Producer<IRecentStatsWriter> recentStatsWriterProducer) {
    StatsAcc.recentStatsWriterProducer = recentStatsWriterProducer;
  }

  public static IOverallStatsWriter getOverallStatsWriter() {
    return overallStatsWriterProducer.produce();
  }

  public static IRecentStatsWriter getRecentStatsWriter() {
    return recentStatsWriterProducer.produce();
  }
}
