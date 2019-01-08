package eng.jAtcSim.lib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.lib.airplanes.moods.Mood;
import eng.jAtcSim.lib.global.ETime;
import eng.jAtcSim.lib.newStats.model.ArrivalDepartureModel;
import eng.jAtcSim.lib.newStats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.lib.newStats.properties.CounterProperty;
import eng.jAtcSim.lib.newStats.properties.StatisticProperty;

public class Collector {
  private final ETime fromTime;
  private final ETime toTime;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesInSim;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesUnderApp;
  private final ArrivalDepartureModel<CounterProperty> runwayMovements;
  private final ArrivalDepartureModel<StatisticProperty> finishedPlanesDelays;
  private final ArrivalDepartureModel<IList<Mood>> finishedPlanesMoods;
  private final StatisticProperty holdingPointDelayStats;

  public Collector(ETime fromTime, ETime toTime) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.planesInSim = new ArrivalDepartureTotalModel<>(
        new StatisticProperty(), new StatisticProperty(), new StatisticProperty());
    this.planesUnderApp = new ArrivalDepartureTotalModel<>(new StatisticProperty(), new StatisticProperty(), new StatisticProperty());
    this.runwayMovements = new ArrivalDepartureModel<>(new CounterProperty(), new CounterProperty());
    this.finishedPlanesDelays = new ArrivalDepartureModel<>(new StatisticProperty(), new StatisticProperty());
    this.finishedPlanesMoods = new ArrivalDepartureModel<>(new EList<>(), new EList<>());
    this.holdingPointDelayStats = new StatisticProperty();
  }

  public ETime getFromTime() {
    return fromTime;
  }

  public ETime getToTime() {
    return toTime;
  }

  public ArrivalDepartureTotalModel<StatisticProperty> getPlanesInSim() {
    return planesInSim;
  }

  public ArrivalDepartureTotalModel<StatisticProperty> getPlanesUnderApp() {
    return planesUnderApp;
  }

  public ArrivalDepartureModel<CounterProperty> getRunwayMovements() {
    return runwayMovements;
  }

  public ArrivalDepartureModel<StatisticProperty> getFinishedPlanesDelays() {
    return finishedPlanesDelays;
  }

  public ArrivalDepartureModel<IList<Mood>> getFinishedPlanesMoods() {
    return finishedPlanesMoods;
  }

  public StatisticProperty getHoldingPointDelayStats() {
    return holdingPointDelayStats;
  }
}
