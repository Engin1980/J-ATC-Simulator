package eng.jAtcSim.lib.stats;

import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.stats.read.StatsView;
import eng.jAtcSim.lib.stats.read.shared.*;
import eng.jAtcSim.lib.stats.read.specific.*;
import eng.jAtcSim.lib.stats.read.specific.PlaneSubStats;

import java.util.function.Function;

public class ViewMerger {
  public static StatsView merge(IReadOnlyList<StatsView> views) {
    StatsView ret;

    ETime fromTime = views.getFirst().getFromTime();
    ETime toTime = views.getLast().getToTime();

    SecondStats ses = mergeSecondStats(views, fromTime, toTime);
    PlaneStats pcs = mergePlaneStats(views);
    MinMaxMeanCountView mds = mergeMoodStats(views);
    HoldingPointStats hps = mergeHoldingPointStats(views);
    ErrorsStats ers = mergeErrorStats(views);

    ret = new StatsView(fromTime, toTime,
        ses, pcs, mds, hps, ers);
    return ret;
  }

  private static ErrorsStats mergeErrorStats(IReadOnlyList<StatsView> views) {
    ErrorsStats ret;
    ret = new ErrorsStats(
        new MeanView(
            mergeDataView(views, q -> q.getErrors().getAirproxes())),
        new MeanView(
            mergeDataView(views, q -> q.getErrors().getMrvas()))
    );
    return ret;
  }

  private static HoldingPointStats mergeHoldingPointStats(IReadOnlyList<StatsView> views) {
    HoldingPointStats ret;
    ret = new HoldingPointStats(
        new MinMaxMeanCountCurrentView(
            mergeDataView(views, q -> q.getHoldingPoint().getDelay())),
        new MinMaxMeanCountCurrentView(
            mergeDataView(views, q -> q.getHoldingPoint().getCount()))
    );
    return ret;
  }

  private static MinMaxMeanCountView mergeMoodStats(IReadOnlyList<StatsView> views) {
    DataView dv = null;
    for (StatsView view : views) {
      if (dv == null)
        dv = new DataView(view.getPlanesMood());
      else
        dv.mergeWith(view.getPlanesMood());
    }
    return new MinMaxMeanCountView(dv);
  }

  private static SecondStats mergeSecondStats(IReadOnlyList<StatsView> views, ETime fromTime, ETime toTime) {
    SecondStats ret;
    DataView duration = mergeView(views, q -> q.getSecondStats().getDuration());
    ret = new SecondStats(
        ETime.getDifference(toTime, fromTime).getTotalSeconds(),
        duration);
    return ret;
  }

  private static PlaneStats mergePlaneStats(IReadOnlyList<StatsView> views) {
    DataView[] tmp;

    tmp = mergePlanesDataView(views,
        q -> q.getPlanes().getPlanesInSim());
    PlaneSubStats<MinMaxMeanCountCurrentView> inSim = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = mergePlanesDataView(views,
        q -> q.getPlanes().getPlanesUnderApp());
    PlaneSubStats<MinMaxMeanCountCurrentView> underApp = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    tmp = mergePlanesDataView(views,
        q -> q.getPlanes().getFinishedPlanes());
    PlaneSubStats<CountMeanView> finished = new PlaneSubStats<>(new CountMeanView(tmp[0]), new CountMeanView(tmp[1]), new CountMeanView(tmp[2]));

    tmp = mergePlanesDataView(views,
        q -> q.getPlanes().getDelay());
    PlaneSubStats<MinMaxMeanCountCurrentView> delay = new PlaneSubStats<>(new MinMaxMeanCountCurrentView(tmp[0]), new MinMaxMeanCountCurrentView(tmp[1]), new MinMaxMeanCountCurrentView(tmp[2]));

    PlaneStats ret;
    ret = new PlaneStats(inSim, underApp, finished, delay);
    return ret;
  }

  private static <T extends DataView> DataView[] mergePlanesDataView(IReadOnlyList<StatsView> writeSets,
                                                                     Function<StatsView, PlaneSubStats<T>> selector) {
    DataView[] ret = new DataView[3];
    ret[0] = mergeDataView(writeSets, q -> selector.apply(q).getArrivals());
    ret[1] = mergeDataView(writeSets, q -> selector.apply(q).getDepartures());
    ret[2] = new DataView(ret[0]);
    ret[2].mergeWith(ret[1]);
    return ret;
  }

  private static DataView mergeView(IReadOnlyList<StatsView> views, Function<StatsView, DataView> selector) {
    DataView ret = null;
    for (StatsView view : views) {
      DataView dv = selector.apply(view);
      if (ret == null)
        ret = new DataView(dv);
      else
        ret.mergeWith(dv);
    }
    return ret;
  }

  private static DataView mergeDataView(IReadOnlyList<StatsView> views, Function<StatsView, DataView> selector) {
    DataView ret = null;
    for (StatsView view : views) {
      DataView dv = selector.apply(view);
      if (ret == null)
        ret = new DataView(dv);
      else
        ret.mergeWith(dv);
    }
    return ret;
  }

}
