package eng.jAtcSim.newLib.stats.model;

import eng.eSystem.validation.EAssert;
import eng.newXmlUtils.annotations.XmlConstructor;
import exml.annotations.XConstructor;

public class ArrivalDepartureTotalModel<T> extends ArrivalDepartureModel<T> {
  private T total;

  @XConstructor
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
