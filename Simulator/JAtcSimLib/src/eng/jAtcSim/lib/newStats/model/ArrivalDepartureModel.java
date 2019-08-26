package eng.jAtcSim.lib.newStats.model;

import eng.eSystem.validation.Validator;
;

public class ArrivalDepartureModel<T> {
  private T arrivals;
  private T departures;

  @XmlConstructor
  protected ArrivalDepartureModel() {
  }

  public ArrivalDepartureModel(T arrivals, T departures) {
    Validator.isNotNull(arrivals);
    Validator.isNotNull(departures);
    this.arrivals = arrivals;
    this.departures = departures;
  }

  public T getArrivals() {
    return arrivals;
  }

  public T getDepartures() {
    return departures;
  }
}
