package eng.jAtcSim.newLib.stats;

import eng.eSystem.Producer;
import eng.eSystem.collections.*;

import static eng.eSystem.utilites.FunctionShortcuts.*;

public class StatsAcc {

  private static Producer<StatsProvider> statsProviderProducer;

  private static void setStatsProviderProducer(Producer<StatsProvider> statsProviderProducer) {
    StatsAcc.statsProviderProducer = statsProviderProducer;
  }

  public static StatsProvider getStatsProvider(){
    return statsProviderProducer.produce();
  }

}
