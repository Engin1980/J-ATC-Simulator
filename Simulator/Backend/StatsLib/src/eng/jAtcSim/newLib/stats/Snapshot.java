package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.IList;
import eng.eSystem.collections.IReadOnlyList;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureModel;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.newLib.stats.properties.MMM;
import eng.jAtcSim.newLib.stats.properties.StatisticProperty;
import exml.IXPersistable;

public class Snapshot implements IXPersistable {
  public static Snapshot createMerge(IReadOnlyList<Snapshot> snapshots) {
    Snapshot ret = new Snapshot();
    ret.time = mergeTime(snapshots.select(q -> q.time));
    ret.planesInSim = mergeArrivalDepartureTotalModelOfMMM(snapshots.select(q -> q.planesInSim));
    ret.planesUnderApp = mergeArrivalDepartureTotalModelOfMMM(snapshots.select(q -> q.planesUnderApp));
    ret.runwayMovementsPerHour = mergeArrivalDepartureTotalModelOfDoubleAsMean(snapshots.select(q -> q.runwayMovementsPerHour));
    ret.finishedPlanesDelays = mergeArrivalDepartureTotalModelOfMMM(snapshots.select(q -> q.finishedPlanesDelays));
    ret.finishedPlanesMoods = mergeArrivalDepartureTotalModelOfMMM(snapshots.select(q -> q.finishedPlanesMoods));
    ret.holdingPointDelayStats = MMM.createMerge(snapshots.select(q -> q.holdingPointDelayStats));
    ret.mrvaErrorsCount = snapshots.sumInt(q -> q.mrvaErrorsCount);
    ret.airproxErrorsCount = snapshots.sumInt(q -> q.airproxErrorsCount);
    ret.holdingPointMaximumCount = snapshots.maxInt(q -> q.holdingPointMaximumCount);

    return ret;
  }

  public static Snapshot of(Collector collector) {
    int totalSeconds = collector.getToTime().getValue() - collector.getFromTime().getValue();
    Snapshot ret = new Snapshot();
    ret.time = collector.getFromTime().addSeconds(totalSeconds / 2);

    ret.planesInSim = new ArrivalDepartureTotalModel<>(
        collector.getPlanesInSim().getArrivals().toMMM(),
        collector.getPlanesInSim().getDepartures().toMMM(),
        collector.getPlanesInSim().getTotal().toMMM());

    ret.planesUnderApp = new ArrivalDepartureTotalModel<>(
        collector.getPlanesUnderApp().getArrivals().toMMM(),
        collector.getPlanesUnderApp().getDepartures().toMMM(),
        collector.getPlanesUnderApp().getTotal().toMMM());

    ret.runwayMovementsPerHour = convertRunwayMovements(collector, totalSeconds);
    ret.finishedPlanesDelays = convertFinishedPlanesDelays(collector.getFinishedPlanesDelays());
    ret.finishedPlanesMoods = convertFinishedPlanesMoods(collector.getFinishedPlanesMoods());
    ret.holdingPointDelayStats = collector.getHoldingPointDelayStats().toMMM();

    ret.mrvaErrorsCount = collector.getErrors().getMrvaErros().getCount();
    ret.airproxErrorsCount = collector.getErrors().getAirproxErros().getCount();

    ret.holdingPointMaximumCount = collector.getHoldingPointMaximumCount();

    return ret;
  }

  private static EDayTimeStamp mergeTime(IReadOnlyList<EDayTimeStamp> times) {
    int meanOfSeconds = (int) times.mean(q -> (double) q.getValue());
    EDayTimeStamp ret = new EDayTimeStamp(meanOfSeconds);
    return ret;
  }

  private static ArrivalDepartureTotalModel<MMM> mergeArrivalDepartureTotalModelOfMMM(IList<ArrivalDepartureTotalModel<MMM>> models) {
    ArrivalDepartureTotalModel<MMM> ret = new ArrivalDepartureTotalModel<>(
        MMM.createMerge(models.select(q -> q.getArrivals())),
        MMM.createMerge(models.select(q -> q.getDepartures())),
        MMM.createMerge(models.select(q -> q.getTotal())));
    return ret;
  }

  private static ArrivalDepartureTotalModel<Double> mergeArrivalDepartureTotalModelOfDoubleAsMean(IList<ArrivalDepartureTotalModel<Double>> models) {
    ArrivalDepartureTotalModel<Double> ret = new ArrivalDepartureTotalModel<>(
        models.select(q -> q.getArrivals()).mean(q -> q),
        models.select(q -> q.getDepartures()).mean(q -> q),
        models.select(q -> q.getArrivals()).mean(q -> q));
    return ret;
  }

  private static ArrivalDepartureTotalModel<Double> convertRunwayMovements(Collector collector, int totalSeconds) {
    ArrivalDepartureTotalModel<Double> ret = new ArrivalDepartureTotalModel<>(
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getArrivals().getCount(), totalSeconds),
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getDepartures().getCount(), totalSeconds),
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getArrivals().getCount() + collector.getRunwayMovements().getDepartures().getCount(), totalSeconds));
    return ret;
  }

  private static ArrivalDepartureTotalModel<MMM> convertFinishedPlanesMoods(ArrivalDepartureModel<IList<MoodResult>> finishedPlanesMoods) {
    IList<MoodResult> tmp;

    // arrivals
    tmp = finishedPlanesMoods.getArrivals();
    MMM arrivals = new MMM(
        tmp.minInt(q -> q.getPoints()),
        tmp.maxInt(q -> q.getPoints()),
        tmp.mean(q -> (double) q.getPoints()));

    tmp = finishedPlanesMoods.getDepartures();
    MMM departures = new MMM(
        tmp.minInt(q -> q.getPoints()),
        tmp.maxInt(q -> q.getPoints()),
        tmp.mean(q -> (double) q.getPoints()));

    tmp = finishedPlanesMoods.getArrivals().union(finishedPlanesMoods.getDepartures());
    MMM total = new MMM(
        tmp.minInt(q -> q.getPoints()),
        tmp.maxInt(q -> q.getPoints()),
        tmp.mean(q -> (double) q.getPoints()));

    ArrivalDepartureTotalModel<MMM> ret = new ArrivalDepartureTotalModel<>(
        arrivals, departures, total);
    return ret;
  }

  private static ArrivalDepartureTotalModel<MMM> convertFinishedPlanesDelays(ArrivalDepartureModel<StatisticProperty> finishedPlanesDelays) {
    ArrivalDepartureTotalModel<MMM> ret;

    MMM arrs = finishedPlanesDelays.getArrivals().toMMM();
    MMM deps = finishedPlanesDelays.getDepartures().toMMM();
    MMM total = finishedPlanesDelays.getArrivals().createMerge(finishedPlanesDelays.getDepartures()).toMMM();

    ret = new ArrivalDepartureTotalModel<>(arrs, deps, total);
    return ret;
  }

  private static double convertMovementsToMovementsPerHour(int movements, int numberOfSeconds) {
    double ret = 3600d / numberOfSeconds * movements;
    return ret;
  }

  private EDayTimeStamp time;
  private ArrivalDepartureTotalModel<MMM> planesInSim;
  private ArrivalDepartureTotalModel<MMM> planesUnderApp;
  private ArrivalDepartureTotalModel<Double> runwayMovementsPerHour;
  private ArrivalDepartureTotalModel<MMM> finishedPlanesDelays;
  private ArrivalDepartureTotalModel<MMM> finishedPlanesMoods;
  private MMM holdingPointDelayStats;
  private int mrvaErrorsCount;
  private int airproxErrorsCount;
  private int holdingPointMaximumCount;

  public int getAirproxErrorsCount() {
    return airproxErrorsCount;
  }

  public ArrivalDepartureTotalModel<MMM> getFinishedPlanesDelays() {
    return finishedPlanesDelays;
  }

  public ArrivalDepartureTotalModel<MMM> getFinishedPlanesMoods() {
    return finishedPlanesMoods;
  }

  public MMM getHoldingPointDelayStats() {
    return holdingPointDelayStats;
  }

  public int getHoldingPointMaximumCount() {
    return this.holdingPointMaximumCount;
  }

  public int getMrvaErrorsCount() {
    return mrvaErrorsCount;
  }

  public ArrivalDepartureTotalModel<MMM> getPlanesInSim() {
    return planesInSim;
  }

  public ArrivalDepartureTotalModel<MMM> getPlanesUnderApp() {
    return planesUnderApp;
  }

  public ArrivalDepartureTotalModel<Double> getRunwayMovementsPerHour() {
    return runwayMovementsPerHour;
  }

  public EDayTimeStamp getTime() {
    return time;
  }
}
