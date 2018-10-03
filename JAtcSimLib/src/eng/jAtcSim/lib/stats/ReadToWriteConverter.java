package eng.jAtcSim.lib.stats;

import eng.eSystem.collections.*;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatisticsView;
import eng.jAtcSim.lib.stats.read.shared.*;
import eng.jAtcSim.lib.stats.read.specific.*;
import eng.jAtcSim.lib.stats.write.WriteSet;
import eng.jAtcSim.lib.stats.write.specific.PlanesSubStats;

class ReadToWriteConverter {

  private static IMap<WriteSet, StatisticsView> calculatedMappings = new EMap<>();

  public static IList<StatisticsView> convert(IReadOnlyList<WriteSet> writeSets) {

    IList ret = new EList();

    for (WriteSet writeSet : writeSets) {
      StatisticsView sv;

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

  private static StatisticsView buildView(WriteSet writeSet) {
    StatisticsView sv;
    ETime fromTime = writeSet.fromTime;
    ETime toTime = writeSet.isLive() ? Acc.now().clone() : writeSet.toTime;

    SecondStats ses = buildSecondStats(writeSet);
    PlaneStats pcs = buildPlanesCountStats(writeSet);
    MinMaxMeanCountView mds = buildMoodStats(writeSet);
    HoldingPointStats hps = buildHoldingPointStats(writeSet);
    ErrorsStats ers = buildErrorStats(writeSet);

    sv = new StatisticsView(fromTime, toTime,
        ses, pcs, mds, hps, ers);
    return sv;
  }

  private static MinMaxMeanCountView buildMoodStats(WriteSet writeSet) {
    DataView ret = writeSet.planesMood.toView();
    return new MinMaxMeanCountCurrentView(ret);
  }

  private static ErrorsStats buildErrorStats(WriteSet writeSet) {
    ErrorsStats ret;
    ret = new ErrorsStats(
        new MeanView(writeSet.errors.getAirproxes().toView()),
        new MeanView(writeSet.errors.getMrvas().toView())
    );
    return ret;
  }

  private static HoldingPointStats buildHoldingPointStats(WriteSet writeSet) {
    HoldingPointStats ret;
    ret = new HoldingPointStats(
        new MinMaxMeanCountCurrentView(
            writeSet.holdingPoint.getDelay().toView()),
        new MinMaxMeanCountCurrentView(
            writeSet.holdingPoint.getCount().toView())
    );
    return ret;
  }

  private static PlaneStats buildPlanesCountStats(WriteSet writeSet) {
    DataView[] tmp;

    tmp = buildPlanesDataView(writeSet.planes.getPlanesInSim());
    PlaneSubStats<MinMaxMeanCountCurrentView> inSim = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getPlanesUnderApp());
    PlaneSubStats<MinMaxMeanCountCurrentView> underApp = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getFinishedPlanes());
    PlaneSubStats<CountMeanView> finished = new PlaneSubStats<>(new CountMeanView(tmp[0]), new CountMeanView(tmp[1]), new CountMeanView(tmp[2]));

    tmp = buildPlanesDataView(writeSet.planes.getDelay());
    PlaneSubStats<MinMaxMeanCountCurrentView> delay = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    PlaneStats ret;
    ret = new PlaneStats(inSim, underApp, finished, delay);
    return ret;
  }

  private static SecondStats buildSecondStats(WriteSet writeSet) {
    ETime toTime = writeSet.isLive() ? Acc.now().clone() : writeSet.toTime;
    int totalSeconds = ETime.getDifference(toTime, writeSet.fromTime).getTotalSeconds();
    SecondStats ret = new SecondStats(totalSeconds, writeSet.secondStats.getDuration().toView());
    return ret;
  }

  private static DataView[] buildPlanesDataView(PlanesSubStats subStats) {
    DataView[] ret = new DataView[3];
    ret[0] = subStats.getArrivals().toView();
    ret[1] = subStats.getDepartures().toView();
    ret[2] = new DataView(ret[0]);
    ret[2].mergeWith(ret[1]);
    return ret;
  }
}
