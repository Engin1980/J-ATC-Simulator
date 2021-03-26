package eng.jAtcSim.newLib.stats.model;

import eng.eSystem.validation.EAssert;
import eng.jAtcSim.newLib.shared.PostContracts;

import exml.IXPersistable;
import exml.annotations.XConstructor;

public class ArrivalDepartureModel<T> implements IXPersistable {
  private T arrivals;
  private T departures;

  @XConstructor

  protected ArrivalDepartureModel() {
    PostContracts.register(this, () -> arrivals != null && departures != null);
  }

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
