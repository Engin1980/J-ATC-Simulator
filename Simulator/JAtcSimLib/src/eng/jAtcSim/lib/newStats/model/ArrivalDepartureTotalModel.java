package eng.jAtcSim.lib.newStats.model;

import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;

public class ArrivalDepartureTotalModel<T> extends ArrivalDepartureModel<T> {
  private T total;

  @XmlConstructor
  protected ArrivalDepartureTotalModel() {
    super();
  }

  public ArrivalDepartureTotalModel(T arrivals, T departures, T total) {
    super(arrivals, departures);
    Validator.isNotNull(total);
    this.total = total;
  }

  public T getTotal() {
    return total;
  }
}
