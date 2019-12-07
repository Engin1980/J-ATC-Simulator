package eng.jAtcSim.newLib.newStats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.airplanes.moods.MoodResult;
import eng.jAtcSim.newLib.global.ETime;
import eng.jAtcSim.newLib.newStats.model.ArrivalDepartureModel;
import eng.jAtcSim.newLib.newStats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.newLib.newStats.model.ErrorsModel;
import eng.jAtcSim.newLib.newStats.properties.CounterProperty;
import eng.jAtcSim.newLib.newStats.properties.StatisticProperty;

public class Collector {
  private final ETime fromTime;
  private final ETime toTime;
  private final StatisticProperty busyCounter;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesInSim;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesUnderApp;
  private final ArrivalDepartureModel<CounterProperty> runwayMovements;
  private final ArrivalDepartureModel<StatisticProperty> finishedPlanesDelays;
  private final ArrivalDepartureModel<IList<MoodResult>> finishedPlanesMoods;
  private final ErrorsModel errors;
  private final StatisticProperty holdingPointDelayStats;
  private int holdingPointMaximumCount = 0;

  public Collector(ETime fromTime, ETime toTime) {
    this.fromTime = fromTime;
    this.toTime = toTime;
    this.busyCounter = new StatisticProperty();
    this.planesInSim = new ArrivalDepartureTotalModel<>(
        new StatisticProperty(), new StatisticProperty(), new StatisticProperty());
    this.planesUnderApp = new ArrivalDepartureTotalModel<>(new StatisticProperty(), new StatisticProperty(), new StatisticProperty());
    this.runwayMovements = new ArrivalDepartureModel<>(new CounterProperty(), new CounterProperty());
    this.finishedPlanesDelays = new ArrivalDepartureModel<>(new StatisticProperty(), new StatisticProperty());
    this.finishedPlanesMoods = new ArrivalDepartureModel<>(new EList<>(), new EList<>());
    this.holdingPointDelayStats = new StatisticProperty();
    this.errors = new ErrorsModel();
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

  public ArrivalDepartureModel<IList<MoodResult>> getFinishedPlanesMoods() {
    return finishedPlanesMoods;
  }

  public StatisticProperty getHoldingPointDelayStats() {
    return holdingPointDelayStats;
  }

  public ErrorsModel getErrors() {
    return errors;
  }

  public StatisticProperty getBusyCounter() {
    return busyCounter;
  }

  public int getHoldingPointMaximumCount() {
    return this.holdingPointMaximumCount;
  }

  public void adjustHoldingPointMaximumCount(int currentHoldingPointCount){
    if (currentHoldingPointCount > this.holdingPointMaximumCount)
      this.holdingPointMaximumCount = currentHoldingPointCount;
  }
}
