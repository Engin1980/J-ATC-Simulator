package eng.jAtcSim.newLib.stats;

import eng.eSystem.functionalInterfaces.Producer;

public class StatsAcc {

  private static Producer<StatsProvider> statsProviderProducer;

  public static StatsProvider getStatsProvider() {
    return statsProviderProducer.produce();
  }

  private static void setStatsProviderProducer(Producer<StatsProvider> statsProviderProducer) {
    StatsAcc.statsProviderProducer = statsProviderProducer;
  }

}
