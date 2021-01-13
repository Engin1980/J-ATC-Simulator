package eng.jAtcSim.newLib.stats;

import eng.eSystem.collections.EList;
import eng.eSystem.collections.IList;
import eng.jAtcSim.newLib.mood.MoodResult;
import eng.jAtcSim.newLib.shared.time.EDayTimeStamp;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureModel;
import eng.jAtcSim.newLib.stats.model.ArrivalDepartureTotalModel;
import eng.jAtcSim.newLib.stats.model.ErrorsModel;
import eng.jAtcSim.newLib.stats.properties.CounterProperty;
import eng.jAtcSim.newLib.stats.properties.StatisticProperty;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.IXPersistable;
import exml.annotations.XConstructor;

public class Collector implements IXPersistable {
  private final EDayTimeStamp fromTime;
  private final EDayTimeStamp toTime;
  private final StatisticProperty busyCounter;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesInSim;
  private final ArrivalDepartureTotalModel<StatisticProperty> planesUnderApp;
  private final ArrivalDepartureModel<CounterProperty> runwayMovements;
  private final ArrivalDepartureModel<StatisticProperty> finishedPlanesDelays;
  private final ArrivalDepartureModel<IList<MoodResult>> finishedPlanesMoods;
  private final ErrorsModel errors;
  private final StatisticProperty holdingPointDelayStats;
  private int holdingPointMaximumCount = 0;

  @XConstructor
  @XmlConstructor
  private Collector(){
    fromTime = null;
    toTime = null;
    busyCounter = null;
    planesInSim = null;
    planesUnderApp = null;
    runwayMovements = null;
    finishedPlanesDelays = null;
    finishedPlanesMoods = null;
    errors = null;
    holdingPointDelayStats = null;
  }
  public Collector(EDayTimeStamp fromTime, EDayTimeStamp toTime) {
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

  public void adjustHoldingPointMaximumCount(int currentHoldingPointCount) {
    if (currentHoldingPointCount > this.holdingPointMaximumCount)
      this.holdingPointMaximumCount = currentHoldingPointCount;
  }

  public StatisticProperty getBusyCounter() {
    return busyCounter;
  }

  public ErrorsModel getErrors() {
    return errors;
  }

  public ArrivalDepartureModel<StatisticProperty> getFinishedPlanesDelays() {
    return finishedPlanesDelays;
  }

  public ArrivalDepartureModel<IList<MoodResult>> getFinishedPlanesMoods() {
    return finishedPlanesMoods;
  }

  public EDayTimeStamp getFromTime() {
    return fromTime;
  }

  public StatisticProperty getHoldingPointDelayStats() {
    return holdingPointDelayStats;
  }

  public int getHoldingPointMaximumCount() {
    return this.holdingPointMaximumCount;
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

  public EDayTimeStamp getToTime() {
    return toTime;
  }
}
