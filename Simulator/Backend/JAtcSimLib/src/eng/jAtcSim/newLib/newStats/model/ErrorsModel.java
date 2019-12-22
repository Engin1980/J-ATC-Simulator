package eng.jAtcSim.newLib.area.newStats.model;

import eng.jAtcSim.newLib.area.newStats.properties.CounterProperty;

public class ErrorsModel {
  private CounterProperty mrvaErros;
  private CounterProperty airproxErros;

  public ErrorsModel() {
    this.mrvaErros = new CounterProperty();
    this.airproxErros = new CounterProperty();
  }

  public ErrorsModel(CounterProperty mrvaErros, CounterProperty airproxErros) {
    this.mrvaErros = mrvaErros;
    this.airproxErros = airproxErros;
  }

  public CounterProperty getMrvaErros() {
    return mrvaErros;
  }

  public CounterProperty getAirproxErros() {
    return airproxErros;
  }
}
