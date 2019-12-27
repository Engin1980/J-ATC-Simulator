package eng.jAtcSim.newLib.stats.model;

import eng.eSystem.validation.EAssert;

public class ArrivalDepartureTotalModel<T> extends ArrivalDepartureModel<T> {
  private T total;

  public ArrivalDepartureTotalModel(T arrivals, T departures, T total) {
    super(arrivals, departures);
    EAssert.isNotNull(total);
    this.total = total;
  }

  public T getTotal() {
    return total;
  }
}
