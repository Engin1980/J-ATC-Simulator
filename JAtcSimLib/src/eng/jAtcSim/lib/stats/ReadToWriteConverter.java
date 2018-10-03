package eng.jAtcSim.lib.stats;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.Acc;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatisticsView;
import eng.jAtcSim.lib.stats.read.shared.*;
import eng.jAtcSim.lib.stats.read.specific.ErrorsStats;
import eng.jAtcSim.lib.stats.read.specific.HoldingPointStats;
import eng.jAtcSim.lib.stats.read.specific.MoodStatsItem;
import eng.jAtcSim.lib.stats.read.specific.PlanesCountStats;
import eng.jAtcSim.lib.stats.read.specific.PlanesViewBlock;
import eng.jAtcSim.lib.stats.read.specific.SecondStats;
import eng.jAtcSim.lib.stats.write.WriteSet;
import eng.jAtcSim.lib.stats.write.shared.Record;
import eng.jAtcSim.lib.stats.write.specific.*;

import java.util.function.Function;

class ReadToWriteConverter {
  public static StatisticsView convert(IReadOnlyList<WriteSet> writeSets) {

    ETime fromTime = writeSets.getFirst().fromTime;
    ETime toTime = Acc.now().clone();

    SecondStats ses = buildSecondStats(writeSets, fromTime, toTime);
    PlanesCountStats pcs = buildPlanesCountStats(writeSets);
    MinMaxMeanCountView mds = buildMoodStats(writeSets);
    HoldingPointStats hps = buildHoldingPointStats(writeSets);
    ErrorsStats ers = buildErrorStats(writeSets);

    StatisticsView ret;
    ret = new StatisticsView(writeSets.getFirst().fromTime, Acc.now().clone(),
        ses, pcs, mds, hps, ers);
    return ret;

  }

  private static MinMaxMeanCountView buildMoodStats(IReadOnlyList<WriteSet> writeSets) {
    DataView dv = null;
    for (WriteSet writeSet : writeSets) {
      if (dv == null)
        dv = writeSet.planesMood.toView();
      else
        dv.mergeWith(writeSet.planesMood.toView());
    }
    return new MinMaxMeanCountView(dv);
  }

  private static ErrorsStats buildErrorStats(IReadOnlyList<WriteSet> writeSets) {
    ErrorsStats ret;
    ret = new ErrorsStats(
        new MeanView(
            buildDataView(writeSets, q -> q.errors.getAirproxes())),
        new MeanView(
            buildDataView(writeSets, q -> q.errors.getMrvas()))
    );
    return ret;
  }

  private static HoldingPointStats buildHoldingPointStats(IReadOnlyList<WriteSet> writeSets) {
    HoldingPointStats ret;
    ret = new HoldingPointStats(
        new MinMaxMeanCountCurrentView(
            buildDataView(writeSets, q -> q.holdingPoint.getDelay())),
        new MinMaxMeanCountCurrentView(
            buildDataView(writeSets, q -> q.holdingPoint.getCount()))
    );
    return ret;
  }

  private static PlanesCountStats buildPlanesCountStats(IReadOnlyList<WriteSet> writeSets) {
    DataView[] tmp;

    tmp = buildPlanesDataView(writeSets,
        q -> q.planes.getPlanesInSim());
    PlanesViewBlock<MinMaxMeanCountCurrentView> inSim = new PlanesViewBlock<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSets,
        q -> q.planes.getPlanesUnderApp());
    PlanesViewBlock<MinMaxMeanCountCurrentView> underApp = new PlanesViewBlock<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = buildPlanesDataView(writeSets,
        q -> q.planes.getFinishedPlanes());
    PlanesViewBlock<CountMeanView> finished = new PlanesViewBlock<>(new CountMeanView(tmp[0]), new CountMeanView(tmp[1]), new CountMeanView(tmp[2]));

    tmp = buildPlanesDataView(writeSets,
        q -> q.planes.getDelay());
    PlanesViewBlock<MinMaxMeanCountCurrentView> delay = new PlanesViewBlock<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    PlanesCountStats ret;
    ret = new PlanesCountStats(inSim, underApp, finished, delay);
    return ret;
  }

  private static SecondStats buildSecondStats(IReadOnlyList<WriteSet> writeSets, ETime fromTime, ETime toTime) {
    int totalSeconds = ETime.getDifference(toTime, fromTime).getTotalSeconds();

    DataView dv = buildDataView(writeSets, q -> q.secondStats.getDuration());
    return new SecondStats(totalSeconds, dv);
  }

  private static DataView[] buildPlanesDataView(IReadOnlyList<WriteSet> writeSets,
                                                Function<WriteSet, PlanesSubStats> selector) {
    DataView[] ret = new DataView[3];
    ret[0] = buildDataView(writeSets, q -> selector.apply(q).getArrivals());
    ret[1] = buildDataView(writeSets, q -> selector.apply(q).getDepartures());
    ret[2] = new DataView(ret[0]);
    ret[2].mergeWith(ret[1]);
    return ret;
  }

  private static DataView buildDataView(IReadOnlyList<WriteSet> writeSets, Function<WriteSet, Record> selector) {
    DataView ret = null;
    for (WriteSet writeSet : writeSets) {
      Record r = selector.apply(writeSet);
      if (ret == null)
        ret = r.toView();
      else
        ret.mergeWith(r.toView());
    }

    return ret;
  }
}
