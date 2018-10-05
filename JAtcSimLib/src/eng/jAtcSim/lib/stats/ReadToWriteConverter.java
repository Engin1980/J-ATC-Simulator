package eng.jAtcSim.lib.stats;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatsView;
import eng.jAtcSim.lib.stats.read.shared.*;
import eng.jAtcSim.lib.stats.read.specific.*;
import eng.jAtcSim.lib.stats.write.StatsData;

class ReadToWriteConverter {

  private static IMap<StatsData, StatsView> calculatedMappings = new EMap<>();

  public static IList<StatsView> convert(IReadOnlyList<StatsData> writeSets) {

    IList ret = new EList();

    for (StatsData writeSet : writeSets) {
      StatsView sv;

      sv = calculatedMappings.tryGet(writeSet);
      if (sv == null) {
        sv = buildView(writeSet);
        if (writeSet.isLive() == false)
          calculatedMappings.set(writeSet, sv);
      }
      ret.add(sv);
    }
    return ret;
  }

  private static StatsView buildView(StatsData writeSet) {
    StatsView sv;
    ETime fromTime = writeSet.fromTime;
    ETime toTime = writeSet.isLive() ? Acc.now().clone() : writeSet.toTime;

    SecondStats ses = buildSecondStats(writeSet);
    PlaneStats pcs = buildPlanesCountStats(writeSet);
    MinMaxMeanCountView mds = buildMoodStats(writeSet);
    HoldingPointStats hps = buildHoldingPointStats(writeSet);
    ErrorsStats ers = buildErrorStats(writeSet);

    sv = new StatsView(fromTime, toTime,
        ses, pcs, mds, hps, ers);
    return sv;
  }

  private static MinMaxMeanCountView buildMoodStats(StatsData writeSet) {
    DataView ret = writeSet.planesMood.toView();
    return new MinMaxMeanCountCurrentView(ret);
  }

  private static ErrorsStats buildErrorStats(StatsData writeSet) {
    ErrorsStats ret;
    ret = new ErrorsStats(
        new MeanView(writeSet.errors.getAirproxes().toView()),
        new MeanView(writeSet.errors.getMrvas().toView())
    );
    return ret;
  }

  private static HoldingPointStats buildHoldingPointStats(StatsData writeSet) {
    HoldingPointStats ret;
    ret = new HoldingPointStats(
        new MinMaxMeanCountCurrentView(
            writeSet.holdingPoint.getDelay().toView()),
        new MinMaxMeanCountCurrentView(
            writeSet.holdingPoint.getCount().toView())
    );
    return ret;
  }

  private static PlaneStats buildPlanesCountStats(StatsData writeSet) {
    DataView[] tmp;

    tmp = buildPlanesDataView(writeSet.planes.getPlanesInSim());
    PlanesSubStats<MinMaxMeanCountCurrentView> inSim = new PlanesSubStats<>(
        new MinMaxMeanCountCurrentView(tmp[0]),
        new MinMaxMeanCountCurrentView(tmp[1]),
        new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getPlanesUnderApp());
    PlanesSubStats<MinMaxMeanCountCurrentView> underApp = new PlanesSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getFinishedPlanes());
    PlanesSubStats<CountMeanView> finished = new PlanesSubStats<>(new CountMeanView(tmp[0]), new CountMeanView(tmp[1]), new CountMeanView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getDelay());
    PlanesSubStats<MinMaxMeanCountCurrentView> delay = new PlanesSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    PlaneStats ret;
    ret = new PlaneStats(inSim, underApp, finished, delay);
    return ret;
  }

  private static SecondStats buildSecondStats(StatsData writeSet) {
    ETime toTime = writeSet.isLive() ? Acc.now().clone() : writeSet.toTime;
    int totalSeconds = ETime.getDifference(toTime, writeSet.fromTime).getTotalSeconds();
    SecondStats ret = new SecondStats(totalSeconds, writeSet.secondStats.getDuration().toView());
    return ret;
  }

  private static DataView[] buildPlanesDataView(eng.jAtcSim.lib.stats.write.specific.PlanesSubStats subStats) {
    DataView[] ret = new DataView[3];
    ret[0] = subStats.getArrivals().toView();
    ret[1] = subStats.getDepartures().toView();
    ret[2] = subStats.getTogether().toView();
    return ret;
  }
}
