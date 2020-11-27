package eng.jAtcSim.newLib.stats.model;

import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.annotations.XmlConstructor;

public class ArrivalDepartureTotalModel<T> extends ArrivalDepartureModel<T> {
  private T total;

  @XmlConstructor
  private ArrivalDepartureTotalModel(){
    super();
  }

  public ArrivalDepartureTotalModel(T arrivals, T departures, T total) {
    super(arrivals, departures);
    EAssert.isNotNull(total);
    this.total = total;
  }

  public T getTotal() {
    return total;
  }
}
