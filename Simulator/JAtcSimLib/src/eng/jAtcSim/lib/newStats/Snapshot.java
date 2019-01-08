package eng.jAtcSim.lib.newStats;

import eng.eSystem.collections.IList;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.newStats.model.ArrivalDepartureModel;
import eng.jAtcSim.lib.newStats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.lib.newStats.properties.MMM;
import eng.jAtcSim.lib.newStats.properties.StatisticProperty;

public class Snapshot {
  private ETime time;
  private ArrivalDepartureTotalModel<MMM> planesInSim;
  private ArrivalDepartureTotalModel<MMM> planesUnderApp;
  private ArrivalDepartureTotalModel<Double> runwayMovementsPerHour;
  private ArrivalDepartureTotalModel<MMM> finishedPlanesDelays;
  private ArrivalDepartureTotalModel<MMM> finishedPlanesMoods;
  private MMM holdingPointDelayStats;

  public static Snapshot of(Collector collector) {
    int totalSeconds = collector.getToTime().getTotalSeconds() - collector.getFromTime().getTotalSeconds();
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

    ret.runwayMovementsPerHour = new ArrivalDepartureTotalModel<>(
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getArrivals().getCount(), totalSeconds),
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getDepartures().getCount(), totalSeconds),
        convertMovementsToMovementsPerHour(collector.getRunwayMovements().getArrivals().getCount() + collector.getRunwayMovements().getDepartures().getCount(), totalSeconds));

    ret.finishedPlanesDelays = convertFinishedPlanesDelays(collector.getFinishedPlanesDelays());
    ret.finishedPlanesMoods = convertFinishedPlanesMoods(collector.getFinishedPlanesMoods());

    return ret;
  }

  private static ArrivalDepartureTotalModel<MMM> convertFinishedPlanesMoods(ArrivalDepartureModel<IList<Mood>> finishedPlanesMoods) {
    return null;
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
    double ret = 60d / numberOfSeconds * movements;
    return ret;
  }



  @XmlConstructor
  private Snapshot() {
  }
}
