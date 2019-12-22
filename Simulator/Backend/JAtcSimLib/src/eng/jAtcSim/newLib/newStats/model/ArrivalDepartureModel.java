package eng.jAtcSim.newLib.area.newStats.model;

import eng.eSystem.validation.Validator;
import eng.eSystem.xmlSerialization.annotations.XmlConstructor;
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
