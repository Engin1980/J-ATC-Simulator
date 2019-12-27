package eng.jAtcSim.newLib.stats.model;

import eng.eSystem.validation.EAssert;

public class ArrivalDepartureModel<T> {
  private T arrivals;
  private T departures;

  public ArrivalDepartureModel(T arrivals, T departures) {
    EAssert.isNotNull(arrivals);
    EAssert.isNotNull(departures);
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
